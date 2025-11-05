// app/src/main/java/com/example/bugmemo/data/NotesRepository.kt
package com.example.bugmemo.data

// ★ keep: seed で Flow の最初の値を読むために使用(kotlinx.coroutines.flow.first)
// ★ keep: Paging 3 は IF のデフォルト実装で使用（拡張関数は削除・内包）
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ──────────────────────────────────────────────────────────────
// Flow ベースのリポジトリIF（← このファイルは IF だけ持つ）
// ──────────────────────────────────────────────────────────────
interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun observeFolders(): Flow<List<Folder>>
    suspend fun getNote(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun deleteNote(id: Long)
    suspend fun addFolder(name: String): Long
    suspend fun deleteFolder(id: Long)
    suspend fun setStarred(id: Long, starred: Boolean)

    // ★ Added: 件数の監視／単発取得 API（UI のバッジ表示や統計に使用）
    fun observeNoteCount(): Flow<Long>
    fun observeFolderCount(): Flow<Long>
    suspend fun countNotes(): Long
    suspend fun countFolders(): Long
    suspend fun countNotesInFolder(folderId: Long): Long

    // ★ Added: バルク挿入（デバッグシード／インポート機能などで使用）
    suspend fun insertAllNotes(notes: List<Note>): List<Long>
    suspend fun insertAllFolders(folders: List<Folder>): List<Long>

    // ─────────── Paging 3 IF（デフォルト実装付き）───────────
    // ★ keep: 全件ページング（簡易版デフォルト）
    //   - Room 実装は override で Pager 版に差し替え可能
    @Suppress("UNUSED_PARAMETER")
    fun pagedNotes(pageSize: Int = 30): Flow<PagingData<Note>> = observeNotes()
        .distinctUntilChanged()
        .map { list -> PagingData.from(list) }

    // ★ keep: 検索ページング（簡易版デフォルト）
    //   - Room 実装は override で FTS/LIKE の PagingSource を使う実装に差し替え可能
    @Suppress("UNUSED_PARAMETER") // ★ keep
    fun pagedSearch(query: String, pageSize: Int = 30): Flow<PagingData<Note>> = searchNotes(query)
        .distinctUntilChanged()
        .map { list -> PagingData.from(list) }

    // ★ Added: フォルダ絞り込み付きページング（簡易版デフォルト）
    //   - Room 実装では DAO の pagingSource(folderId) を使う Pager に override してください
    @Suppress("UNUSED_PARAMETER")
    fun pagedNotesByFolder(folderId: Long?, pageSize: Int = 30): Flow<PagingData<Note>> = observeNotes()
        .distinctUntilChanged()
        .map { list -> list.filter { folderId == null || it.folderId == folderId } }
        .map { filtered -> PagingData.from(filtered) }
}

/* ===============================
   ★ keep: Repository の最小 API だけで動く “シード” ユーティリティ
   - 既存の実装(RoomNotesRepository など)を変更せずに利用可能（拡張関数）
   - 起動時に observeNotes()/observeFolders() の初期値を確認して空なら投入
   =============================== */
data class SeedNote(
    val title: String,
    val content: String,
    val folderName: String? = null,
    val starred: Boolean = false,
)

suspend fun NotesRepository.seedIfEmpty(
    folders: List<String> = listOf("Inbox"),
    notes: List<SeedNote> = emptyList(),
) {
    // 既存データがあれば終了（重複投入防止）
    val hasNotes = observeNotes().first().isNotEmpty()
    if (hasNotes) return

    // フォルダ投入（既存名があって addFolder が 0L を返しても安全に継続）
    val nameToId = mutableMapOf<String, Long>()
    for (name in folders.distinct()) {
        val id = addFolder(name)
        if (id != 0L) {
            nameToId[name] = id
        } else {
            // 0L の場合でも致命的ではない（後続ノートは folderId=null で挿入）
        }
    }

    val now = System.currentTimeMillis()
    for (n in notes) {
        val folderId = n.folderName?.let { nameToId[it] }
        // 見つからなければ null
        val insertedId = upsert(
            Note(
                id = 0L,
                title = n.title,
                content = n.content,
                folderId = folderId,
                createdAt = now,
                updatedAt = now,
                isStarred = n.starred,
            ),
        )
        if (n.starred && insertedId != 0L) {
            setStarred(insertedId, true)
        }
    }
}

/* ===============================
   ★ Removed: Paging 3 の拡張関数（pagingAllNotes/pagingSearch、拡張の pagedNotes/pagedSearch）
   - 役割は IF のデフォルト実装へ内包しました。
   - Room 実装は Repository クラス側で override 済み（Pager 使用）に差し替えてください。
   =============================== */
