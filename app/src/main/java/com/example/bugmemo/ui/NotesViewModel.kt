// app/src/main/java/com/example/bugmemo/ui/NotesViewModel.kt
@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.example.bugmemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.data.NotesRepository
import com.example.bugmemo.data.SeedNote
import com.example.bugmemo.data.prefs.SettingsRepository
import com.example.bugmemo.data.remote.GistFileContent
import com.example.bugmemo.data.remote.GistRequest
import com.example.bugmemo.data.remote.GistService
import com.example.bugmemo.data.seedIfEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository,
    private val settings: SettingsRepository,
    private val gistService: GistService,
) : ViewModel() {

    // ─────────── UIイベント ───────────
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
        data class UndoDelete(val text: String) : UiEvent
        data object NavigateToEditor : UiEvent
    }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private fun sendEvent(event: UiEvent) {
        _events.trySend(event)
    }

    private var lastDeleted: Note? = null

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
        viewModelScope.launch {
            runCatching { settings.setLastQuery(q) }
                .onFailure { e -> sendEvent(UiEvent.Message("検索語の保存に失敗しました: ${e.message}")) }
        }
    }

    private val debouncedQuery: Flow<String> =
        query.map { it.trim() }.debounce(250).distinctUntilChanged()

    private val _filterFolderId = MutableStateFlow<Long?>(null)
    val filterFolderId: StateFlow<Long?> = _filterFolderId.asStateFlow()

    fun setFolderFilter(id: Long?) {
        _filterFolderId.value = id
        viewModelScope.launch {
            runCatching { settings.setFilterFolderId(id) }
                .onFailure { e -> sendEvent(UiEvent.Message("絞り込みの保存に失敗しました: ${e.message}")) }
        }
    }

    init {
        viewModelScope.launch {
            settings.filterFolderId.collect { if (it != _filterFolderId.value) _filterFolderId.value = it }
        }
        viewModelScope.launch {
            settings.lastQuery.collect { if (it != _query.value) _query.value = it }
        }
    }

    private val pageSize = 50
    val pagedNotes: Flow<PagingData<Note>> =
        combine(debouncedQuery, filterFolderId) { q, folderId -> q to folderId }
            .flatMapLatest { (q, folderId) ->
                if (q.isBlank()) {
                    repo.pagedNotesByFolder(folderId = folderId, pageSize = pageSize)
                } else {
                    repo.pagedSearch(query = q, pageSize = pageSize)
                }
            }
            .cachedIn(viewModelScope)

    val folders: StateFlow<List<Folder>> =
        repo.observeFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    fun handleSharedText(sharedText: String) {
        if (sharedText.isBlank()) return
        newNote()
        setEditingContent(sharedText)

        val autoTitle = sharedText.trim().lineSequence().firstOrNull()?.take(30) ?: "Shared Note"
        setEditingTitle(if (sharedText.length > 30) "$autoTitle..." else autoTitle)

        sendEvent(UiEvent.NavigateToEditor)
    }

    fun loadNote(id: Long) {
        viewModelScope.launch {
            runCatching { repo.getNote(id) }
                .onSuccess { _editing.value = it }
                .onFailure { sendEvent(UiEvent.Message("ノート読込に失敗しました")) }
        }
    }

    fun newNote() {
        val now = System.currentTimeMillis()
        _editing.value = Note(
            id = 0L,
            title = "",
            content = "",
            folderId = null,
            createdAt = now,
            updatedAt = now,
            isStarred = false,
            imagePaths = emptyList(),
        )
    }

    fun setEditingTitle(text: String) {
        _editing.update { it?.copy(title = text) }
    }
    fun setEditingContent(text: String) {
        _editing.update { it?.copy(content = text) }
    }
    fun setEditingFolder(folderId: Long?) {
        _editing.update { it?.copy(folderId = folderId) }
    }

    fun addImagePath(path: String) {
        _editing.update { it?.copy(imagePaths = it.imagePaths + path) }
    }

    fun removeImagePath(path: String) {
        _editing.update { it?.copy(imagePaths = it.imagePaths - path) }
    }

    fun saveEditing() {
        val note = _editing.value ?: return
        viewModelScope.launch {
            runCatching {
                val id = repo.upsert(note)
                if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
                sendEvent(UiEvent.Message("保存しました"))
            }.onFailure { sendEvent(UiEvent.Message("保存に失敗しました")) }
        }
    }

    @Suppress("unused")
    fun deleteEditing() {
        val id = _editing.value?.id ?: return
        viewModelScope.launch {
            runCatching {
                lastDeleted = _editing.value
                repo.deleteNote(id)
                _editing.value = null
                sendEvent(UiEvent.UndoDelete("削除しました（元に戻す）"))
            }.onFailure { sendEvent(UiEvent.Message("削除に失敗しました")) }
        }
    }

    fun undoDelete() {
        val note = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching {
                repo.upsert(note.copy(id = 0L, updatedAt = System.currentTimeMillis()))
                sendEvent(UiEvent.Message("復元しました"))
            }.onFailure { sendEvent(UiEvent.Message("復元に失敗しました")) }
        }
    }

    fun toggleStar(noteId: Long, current: Boolean) {
        viewModelScope.launch {
            runCatching {
                repo.setStarred(noteId, !current)
                sendEvent(UiEvent.Message(if (current) "スターを外しました" else "スターを付けました"))
            }.onFailure { sendEvent(UiEvent.Message("スター変更に失敗しました")) }
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            runCatching { repo.addFolder(name) }
                .onSuccess { if (it != 0L) sendEvent(UiEvent.Message("フォルダを追加しました")) }
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            runCatching {
                repo.deleteFolder(id)
                if (filterFolderId.value == id) _filterFolderId.value = null
                sendEvent(UiEvent.Message("フォルダを削除しました"))
            }
        }
    }

    @Suppress("unused")
    fun seedIfEmpty(folders: List<String> = listOf("Inbox"), notes: List<SeedNote> = emptyList()) {
        viewModelScope.launch {
            runCatching { repo.seedIfEmpty(folders, notes) }
        }
    }

    // ★ Fix: アプリクラッシュの真犯人を修正
    // 1. Suppress("unused") を削除（AllNotesScreenから呼ばれるため）
    // 2. withContext(Dispatchers.IO) でバックグラウンド実行を保証
    // 3. 日付フォーマットを DateTimeFormatter に変更し、警告を解消
    fun syncToGist() {
        val token = settings.githubToken.value
        if (token.isBlank()) {
            sendEvent(UiEvent.Message("GitHub Tokenが設定されていません"))
            return
        }

        viewModelScope.launch {
            sendEvent(UiEvent.Message("Syncing..."))

            // ★ Crash Fix: 重い処理をIOスレッドに逃がす
            runCatching {
                withContext(Dispatchers.IO) {
                    val allNotes = repo.observeNotes().first()
                    // ★ Fix: モダンな DateTimeFormatter を使用
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    val files = allNotes.associate { note ->
                        val filename = "note_${note.id}.md"
                        val content = buildString {
                            appendLine("# ${note.title.ifBlank { "Untitled" }}")
                            appendLine()
                            appendLine(note.content)
                            if (note.imagePaths.isNotEmpty()) {
                                appendLine()
                                appendLine("## Attachments")
                                note.imagePaths.forEach { path ->
                                    appendLine("- $path")
                                }
                            }
                            appendLine()
                            val updatedTime = Instant.ofEpochMilli(note.updatedAt)
                                .atZone(ZoneId.systemDefault())
                                .format(formatter)
                            appendLine("> Last Updated: $updatedTime")
                        }
                        filename to GistFileContent(content)
                    }

                    val nowTime = Instant.now().atZone(ZoneId.systemDefault()).format(formatter)
                    val request = GistRequest(
                        description = "BugMemo Sync - $nowTime",
                        public = false,
                        files = files,
                    )

                    val response = gistService.createGist("token $token", request)
                    response
                }
            }.onSuccess { response ->
                sendEvent(UiEvent.Message("Sync Success! Gist ID: ${response.id}"))
            }.onFailure { e ->
                e.printStackTrace()
                sendEvent(UiEvent.Message("Sync Failed: ${e.message}"))
            }
        }
    }
}
