// app/src/main/java/com/example/bugmemo/ui/NotesViewModel.kt
@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)

package com.example.bugmemo.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.data.NotesRepository
import com.example.bugmemo.data.RoomNotesRepository
import com.example.bugmemo.data.db.AppDatabase
import com.example.bugmemo.data.prefs.SettingsRepository
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

/**
 * NotesViewModel
 * - 検索クエリ / フォルダ絞り込みを DataStore へ保存・復元
 * - 一覧は（検索 × フォルダ）で動的フィルタ
 * - ★ 削除 → Undo 対応を追加
 */
class NotesViewModel(
    private val repo: NotesRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    // ─────────── UIイベント（Snackbar など）───────────
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
        data class UndoDelete(val text: String) : UiEvent   // ★ Added: Undo 用イベント
    }
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ★ Added: 直前に削除したノート（Undo 復元用）を保持
    private var lastDeleted: Note? = null

    // ─────────── 検索クエリ（保存・復元対応）──────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
        // ★ Added: 入力のたびに DataStore へ保存
        viewModelScope.launch { settings.setLastQuery(q) }
    }

    // ─────────── フォルダ絞り込み（保存・復元対応）──────────
    private val _filterFolderId = MutableStateFlow<Long?>(null)
    val filterFolderId: StateFlow<Long?> = _filterFolderId.asStateFlow()

    fun setFolderFilter(id: Long?) {
        _filterFolderId.value = id
        // ★ Added: 変更を DataStore に保存
        viewModelScope.launch { settings.setFilterFolderId(id) }
    }

    // ★ Added: 起動時に DataStore から query / filter を復元
    init {
        // フォルダ絞り込みIDの復元
        viewModelScope.launch {
            settings.filterFolderId
                .distinctUntilChanged()
                .collect { saved -> _filterFolderId.value = saved }
        }
        // 検索クエリの復元
        viewModelScope.launch {
            settings.lastQuery
                .distinctUntilChanged()
                .collect { saved ->
                    if (saved != _query.value) _query.value = saved
                }
        }
    }

    // ─────────── 一覧（検索＋フォルダ絞り込み）───────────
    val notes: StateFlow<List<Note>> =
        combine(query, _filterFolderId) { q, folderId -> q.trim() to folderId }
            .debounce(150)
            .flatMapLatest { (q, folderId) ->
                val base = if (q.isEmpty()) repo.observeNotes() else repo.searchNotes(q)
                if (folderId == null) base
                else base.map { list -> list.filter { it.folderId == folderId } }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ─────────── フォルダ一覧 ───────────
    val folders: StateFlow<List<Folder>> =
        repo.observeFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ─────────── 編集対象 ───────────
    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch { _editing.value = repo.getNote(id) }
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
            isStarred = false
        )
    }

    fun setEditingTitle(text: String) { _editing.update { it?.copy(title = text) } }
    fun setEditingContent(text: String) { _editing.update { it?.copy(content = text) } }
    fun setEditingFolder(folderId: Long?) { _editing.update { it?.copy(folderId = folderId) } }

    fun saveEditing() {
        val note = _editing.value ?: return
        viewModelScope.launch {
            val id = repo.upsert(note)
            if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
            _events.tryEmit(UiEvent.Message("保存しました"))
        }
    }

    // ★ Changed: 削除時に退避＋Undo 可能なイベントを発行
    fun deleteEditing() {
        val id = _editing.value?.id ?: return
        viewModelScope.launch {
            lastDeleted = _editing.value                 // ★ 退避
            repo.deleteNote(id)
            _editing.value = null
            _events.tryEmit(UiEvent.UndoDelete("削除しました（元に戻す）")) // ★ Snackbar 側でアクション表示
        }
    }

    // ★ Added: Undo 実行（Snackbar のアクションから呼ぶ）
    fun undoDelete() {
        val note = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            // 新規として復元（IDはDB側で再採番）
            repo.upsert(
                note.copy(
                    id = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _events.tryEmit(UiEvent.Message("復元しました"))
        }
    }

    fun toggleStar(noteId: Long, current: Boolean) {
        viewModelScope.launch {
            repo.setStarred(noteId, !current)
            _events.tryEmit(UiEvent.Message(if (current) "スターを外しました" else "スターを付けました"))
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            val res = repo.addFolder(name)
            if (res != 0L) _events.tryEmit(UiEvent.Message("フォルダを追加しました"))
            else _events.tryEmit(UiEvent.Message("フォルダ名が不正、または重複しています"))
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            repo.deleteFolder(id)
            _events.tryEmit(UiEvent.Message("フォルダを削除しました"))
        }
    }

    // ─────────── Factory（DI 最小）──────────
    companion object {
        // アプリから DB / Settings を引き当てて VM を作る Factory
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val db = AppDatabase.get(app)
                val notesRepo = RoomNotesRepository(db.noteDao(), db.folderDao())
                val settingsRepo = SettingsRepository.get(app)
                NotesViewModel(notesRepo, settingsRepo)
            }
        }
    }
}
