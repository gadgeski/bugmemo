// app/src/main/java/com/example/bugmemo/ui/NotesViewModel.kt
@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.example.bugmemo.ui

// ★ Added: Repository の“シード”拡張を VM から使うための import
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
import com.example.bugmemo.data.SeedNote
import com.example.bugmemo.data.db.AppDatabase
import com.example.bugmemo.data.prefs.SettingsRepository
import com.example.bugmemo.data.seedIfEmpty
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

/**
 * NotesViewModel
 * - 検索クエリ / フォルダ絞り込みを DataStore へ保存・復元
 * - 一覧は（検索 × フォルダ）で動的フィルタ
 * - 削除→Undo / 失敗時のSnackbar通知
 */
class NotesViewModel(
    private val repo: NotesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    // ─────────── UIイベント（Snackbar など）───────────
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
        data class UndoDelete(val text: String) : UiEvent
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
        // ★ keep: 入力のたびに DataStore へ保存（必要なら保存側を debounce へ変更も可）
        viewModelScope.launch {
            runCatching { settings.setLastQuery(q) }
                .onFailure { e ->
                    _events.tryEmit(UiEvent.Message("検索語の保存に失敗しました: ${e.message ?: "不明なエラー"}"))
                }
        }
    }

    // ★ Added: 検索クエリに debounce + trim + distinctUntilChanged を適用したフロー
    //           UI側は query をそのまま更新してOK。実際の検索切替はこのフローを使います。
    private val debouncedQuery: Flow<String> =
        query
            .map { it.trim() }
            .debounce(250)
            // ★ keep（150–250msは好みで調整可）
            .distinctUntilChanged()
    // ★ keep（これは StateFlow ではなく map/debounce 後の Flow に対して有効）

    // ─────────── フォルダ絞り込み（保存・復元対応）──────────
    private val _filterFolderId = MutableStateFlow<Long?>(null)
    val filterFolderId: StateFlow<Long?> = _filterFolderId.asStateFlow()

    fun setFolderFilter(id: Long?) {
        _filterFolderId.value = id
        // ★ keep: 変更を DataStore に保存（失敗時通知）
        viewModelScope.launch {
            runCatching { settings.setFilterFolderId(id) }
                .onFailure { e ->
                    _events.tryEmit(UiEvent.Message("絞り込みの保存に失敗しました: ${e.message ?: "不明なエラー"}"))
                }
        }
    }

    // ★ Changed: 起動時復元は distinctUntilChanged() を使わず、
    //             「値が変わったときだけ代入」で重複反映を防止（StateFlow 非推奨API回避）
    init {
        // フォルダ絞り込みIDの復元
        viewModelScope.launch {
            settings.filterFolderId
                // .distinctUntilChanged() // ★ Removed: StateFlow 非推奨（下で比較して反映）
                .collect { saved ->
                    if (saved != _filterFolderId.value) {
                        // ★ Added: 値が変わった時だけ反映
                        _filterFolderId.value = saved
                    }
                }
        }
        // 検索クエリの復元
        viewModelScope.launch {
            settings.lastQuery
                // .distinctUntilChanged() // ★ Removed: 同上
                .collect { saved ->
                    if (saved != _query.value) {
                        // ★ keep: 重複回避ロジック
                        _query.value = saved
                    }
                }
        }
    }

    // ─────────── 一覧（検索＋フォルダ絞り込み）───────────
    // ★ Changed: combine の第二引数で StateFlow に distinctUntilChanged() を掛けない
    //            （StateFlow は連続同値をそもそも流さない設計のため）
    val notes: StateFlow<List<Note>> =
        combine(
            debouncedQuery,
            filterFolderId,
            // ★ Changed: _filterFolderId.distinctUntilChanged() → filterFolderId に置換
        ) { q, folderId -> q to folderId }
            .flatMapLatest { (q, folderId) ->
                val base = if (q.isBlank()) repo.observeNotes() else repo.searchNotes(q)
                if (folderId == null) {
                    base
                } else {
                    base.map { list -> list.filter { it.folderId == folderId } }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
        _editing.value =
            Note(
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
                val id = repo.upsert(note) // ★ keep
                if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
                _events.tryEmit(UiEvent.Message("保存しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("保存に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    // ★ keep: 削除→Undo
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

    // ★ keep: Undo 実行
    fun undoDelete() {
        val note = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching {
                repo.upsert(
                    note.copy(
                        id = 0L,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
                _events.tryEmit(UiEvent.Message("復元しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("復元に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    fun toggleStar(
        noteId: Long,
        current: Boolean,
    ) {
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
                repo.deleteFolder(id) // ★ keep
                if (filterFolderId.value == id) _filterFolderId.value = null
                _events.tryEmit(UiEvent.Message("フォルダを削除しました"))
            }.onFailure { e ->
                _events.tryEmit(UiEvent.Message("フォルダ削除に失敗しました: ${e.message ?: "不明なエラー"}"))
            }
        }
    }

    /* ===============================
       ★ Added: データ投入（シード）ヘルパ
       - Repository の拡張関数 seedIfEmpty(...) を VM 経由で呼び出す薄いラッパ
       - MainActivity などから「1回だけ」投入したいときに呼ぶ
       - 既存データがあれば何もしない（重複投入防止は拡張側で実施）
       =============================== */
    // ★ Added: 現状は呼び出し元が未配線のため警告抑制（MainActivity から使うなら外してOK）
    @Suppress("unused")
    fun seedIfEmpty(
        folders: List<String> = listOf("Inbox", "Ideas"),
        notes: List<SeedNote> = listOf(
            // ★ keep: 例として Welcome ノートを 1 件
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
    // ★ Added: ここまで（シードヘルパ）

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
