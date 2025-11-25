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
import com.example.bugmemo.data.seedIfEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NotesViewModel (Hilt Edition)
 * - @HiltViewModel と @Inject constructor により、Factory 不要で自動生成
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    // ─────────── UIイベント ───────────
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
        data class UndoDelete(val text: String) : UiEvent
    }
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // 直前に削除したノート（Undo 復元用）
    private var lastDeleted: Note? = null

    // ─────────── 検索クエリ ──────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
        viewModelScope.launch {
            runCatching { settings.setLastQuery(q) }
                .onFailure { e ->
                    _events.tryEmit(UiEvent.Message("検索語の保存に失敗しました: ${e.message ?: "不明なエラー"}"))
                }
        }
    }

    private val debouncedQuery: Flow<String> =
        query
            .map { it.trim() }
            .debounce(250)
            .distinctUntilChanged()

    // ─────────── フォルダ絞り込み ──────────
    private val _filterFolderId = MutableStateFlow<Long?>(null)
    val filterFolderId: StateFlow<Long?> = _filterFolderId.asStateFlow()

    fun setFolderFilter(id: Long?) {
        _filterFolderId.value = id
        viewModelScope.launch {
            runCatching { settings.setFilterFolderId(id) }
                .onFailure { e ->
                    _events.tryEmit(UiEvent.Message("絞り込みの保存に失敗しました: ${e.message ?: "不明なエラー"}"))
                }
        }
    }

    init {
        // 設定の復元
        viewModelScope.launch {
            settings.filterFolderId.collect { saved ->
                if (saved != _filterFolderId.value) _filterFolderId.value = saved
            }
        }
        viewModelScope.launch {
            settings.lastQuery.collect { saved ->
                if (saved != _query.value) _query.value = saved
            }
        }
    }

    // ─────────── Paging 3 版 ──────────
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

    // ─────────── フォルダ一覧 ───────────
    val folders: StateFlow<List<Folder>> =
        repo.observeFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ─────────── 編集対象 ───────────
    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch {
            runCatching { repo.getNote(id) }
                .onSuccess { _editing.value = it }
                .onFailure { e ->
                    _events.tryEmit(UiEvent.Message("ノート読込に失敗しました: ${e.message ?: "不明なエラー"}"))
                }
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

    fun saveEditing() {
        val note = _editing.value ?: return
        viewModelScope.launch {
            runCatching {
                val id = repo.upsert(note)
                if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
                _events.tryEmit(UiEvent.Message("保存しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("保存に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun deleteEditing() {
        val id = _editing.value?.id ?: return
        viewModelScope.launch {
            runCatching {
                lastDeleted = _editing.value
                repo.deleteNote(id)
                _editing.value = null
                _events.tryEmit(UiEvent.UndoDelete("削除しました（元に戻す）"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("削除に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun undoDelete() {
        val note = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching {
                repo.upsert(note.copy(id = 0L, updatedAt = System.currentTimeMillis()))
                _events.tryEmit(UiEvent.Message("復元しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("復元に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun toggleStar(noteId: Long, current: Boolean) {
        viewModelScope.launch {
            runCatching {
                repo.setStarred(noteId, !current)
                _events.tryEmit(UiEvent.Message(if (current) "スターを外しました" else "スターを付けました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("スター変更に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            runCatching {
                val res = repo.addFolder(name)
                if (res != 0L) {
                    _events.tryEmit(UiEvent.Message("フォルダを追加しました"))
                } else {
                    _events.tryEmit(UiEvent.Message("フォルダ名が不正、または重複しています"))
                }
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("フォルダ追加に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            runCatching {
                repo.deleteFolder(id)
                if (filterFolderId.value == id) _filterFolderId.value = null
                _events.tryEmit(UiEvent.Message("フォルダを削除しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("フォルダ削除に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    // シード投入ヘルパ
    @Suppress("unused")
    fun seedIfEmpty(
        folders: List<String> = listOf("Inbox", "Ideas"),
        notes: List<SeedNote> = listOf(
            SeedNote(
                title = "Welcome to BugMemo",
                content = """
                    これはデバッグ用に自動投入されたサンプルノートです。
                    - 下部ナビから「Search / Folders」を試せます
                    - 右上ショートカットで All Notes に遷移できます
                    - エディタで保存 / 削除 → Undo も確認してみてください
                """.trimIndent(),
                folderName = "Inbox",
                starred = true,
            ),
        ),
    ) {
        viewModelScope.launch {
            runCatching {
                repo.seedIfEmpty(folders = folders, notes = notes)
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("シード投入に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }
}
