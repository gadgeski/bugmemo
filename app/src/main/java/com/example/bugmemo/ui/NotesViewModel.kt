@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)

package com.example.bugmemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.data.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {

    // ★ Added: UI イベント（Snackbar 用）
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
    }
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // 検索
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) { _query.value = q }

    // フォルダフィルタ（将来: Bugs ← Folders 導線で使用予定）
    private val _filterFolderId = MutableStateFlow<Long?>(null)
    fun setFolderFilter(id: Long?) { _filterFolderId.value = id } // ★ Added

    // ノート一覧
    val notes: StateFlow<List<Note>> =
        combine(query, _filterFolderId) { q, folderId -> q.trim() to folderId }
            .debounce(150)
            .flatMapLatest { (q, folderId) ->
                val base = if (q.isEmpty()) repo.observeNotes() else repo.searchNotes(q)
                if (folderId == null) base else base.map { list -> list.filter { it.folderId == folderId } }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // フォルダ一覧
    val folders: StateFlow<List<Folder>> =
        repo.observeFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 編集対象
    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch {
            _editing.value = repo.getNote(id)
        }
    }

    fun newNote() {
        val now = System.currentTimeMillis()
        _editing.value = Note(0L, "", "", null, now, now, false)
    }

    fun setEditingTitle(text: String) { _editing.update { it?.copy(title = text) } }
    fun setEditingContent(text: String) { _editing.update { it?.copy(content = text) } }
    fun setEditingFolder(folderId: Long?) { _editing.update { it?.copy(folderId = folderId) } }

    fun saveEditing() {
        val note = _editing.value ?: return
        viewModelScope.launch {
            val id = repo.upsert(note)
            if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
            _events.tryEmit(UiEvent.Message("保存しました"))        // ★ Added
        }
    }

    fun deleteEditing() {
        val id = _editing.value?.id ?: return
        viewModelScope.launch {
            repo.deleteNote(id)
            _editing.value = null
            _events.tryEmit(UiEvent.Message("削除しました"))        // ★ Added
        }
    }

    fun toggleStar(noteId: Long, current: Boolean) {
        viewModelScope.launch {
            repo.setStarred(noteId, !current)
            _events.tryEmit(UiEvent.Message(if (current) "スターを外しました" else "スターを付けました")) // ★ Added
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            val res = repo.addFolder(name)
            if (res != 0L) _events.tryEmit(UiEvent.Message("フォルダを追加しました"))               // ★ Added
            else _events.tryEmit(UiEvent.Message("フォルダ名が不正、または重複しています"))        // ★ Added
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            repo.deleteFolder(id)
            _events.tryEmit(UiEvent.Message("フォルダを削除しました"))                               // ★ Added
        }
    }
}
