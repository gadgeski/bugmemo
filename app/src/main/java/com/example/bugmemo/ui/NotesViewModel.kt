// app/src/main/java/.../NotesViewModel.kt

@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,              // ★ 追加: debounce 等のプレビューAPI
    kotlinx.coroutines.ExperimentalCoroutinesApi::class // ★ 追加: stateIn/SharingStarted 等で要求される場合あり
)

package com.example.bugmemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.data.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * NotesViewModel（Flowベース版）
 * - 旧API all()/folders()/create()/update()/delete()/find() は使用しない // ★ 置き換え
 * - 新API observeNotes()/observeFolders()/upsert()/deleteNote()/getNote()/setStarred()/addFolder()/deleteFolder() を使用
 */
class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {

    // 検索キーワード
    private val _query = MutableStateFlow("")                         // ★ 追加: 検索状態
    val query: StateFlow<String> = _query.asStateFlow()

    // ノート一覧（検索クエリに応じて Flow を切替）
    val notes: StateFlow<List<Note>> =
        query
            .debounce(150)                                            // タイプ中の揺れを軽減（任意）
            .map { it.trim() }
            .flatMapLatest { q ->
                if (q.isEmpty()) repo.observeNotes()                  // ★ 旧: all() → 新: observeNotes()
                else repo.searchNotes(q)                              // ★ 旧: 手元検索 → 新: searchNotes()
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // フォルダ一覧
    val folders: StateFlow<List<Folder>> =
        repo.observeFolders()                                         // ★ 旧: folders() → 新: observeFolders()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 編集対象（詳細表示やエディタで使う想定）
    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    // ─────────────────────────────────────────────
    // UI アクション
    // ─────────────────────────────────────────────

    fun setQuery(q: String) { _query.value = q }                      // ★ 置き換え: create()/find() ではなく検索用の状態

    fun loadNote(id: Long) {                                          // ★ 旧: find(id) → 新: getNote(id)
        viewModelScope.launch {
            _editing.value = repo.getNote(id)
        }
    }

    fun newNote() {                                                   // ★ 旧: create() → 新: upsert(Note(...))
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

    fun saveEditing() {                                               // ★ 旧: update(...) → 新: upsert(note)
        val note = _editing.value ?: return
        viewModelScope.launch {
            val id = repo.upsert(note)
            if (note.id == 0L && id != 0L) {
                _editing.value = note.copy(id = id)
            }
        }
    }

    fun deleteEditing() {                                             // ★ 旧: delete(id) → 新: deleteNote(id)
        val id = _editing.value?.id ?: return
        viewModelScope.launch { repo.deleteNote(id) }
        _editing.value = null
    }

    fun toggleStar(noteId: Long, current: Boolean) {                  // ★ 追加: 部分更新 setStarred()
        viewModelScope.launch { repo.setStarred(noteId, !current) }
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

    // フォルダ操作
    fun addFolder(name: String) {                                     // ★ suspend を launch で呼ぶ
        viewModelScope.launch { repo.addFolder(name) }                // ← エラー1の解決ポイント
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch { repo.deleteFolder(id) }
    }
}
