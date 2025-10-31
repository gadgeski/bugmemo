// app/src/main/java/com/example/bugmemo/data/NotesRepository.kt
package com.example.bugmemo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
// ★ keep: seed で Flow の最初の値を読むために使用(kotlinx.coroutines.flow.first)

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
    // 既存のまま

    // ★ Added: 件数の監視／単発取得 API（UI のバッジ表示や統計に使用）
    fun observeNoteCount(): Flow<Long>
    fun observeFolderCount(): Flow<Long>
    suspend fun countNotes(): Long
    suspend fun countFolders(): Long
    suspend fun countNotesInFolder(folderId: Long): Long

    // ★ Added: バルク挿入（デバッグシード／インポート機能などで使用）
    suspend fun insertAllNotes(notes: List<Note>): List<Long>
    suspend fun insertAllFolders(folders: List<Folder>): List<Long>
}

/* ===============================
   ★ keep: Repository の最小 API だけで動く “シード” ユーティリティ
   - 既存の実装(RoomNotesRepository など)を変更せずに利用可能（拡張関数）
   - 起動時に observeNotes()/observeFolders() の初期値を確認して空なら投入
   - 使い方（Debug等で一度だけ）:
       lifecycleScope.launch {
           repo.seedIfEmpty(
               folders = listOf("Inbox", "Ideas"),
               notes = listOf(
                   SeedNote("Welcome", "BugMemoへようこそ", folderName = "Inbox", starred = true)
               )
           )
       }
   =============================== */

/** ★ keep: シード用の簡易モデル（フォルダ名でひも付け） */
data class SeedNote(
    val title: String,
    val content: String,
    val folderName: String? = null,
    val starred: Boolean = false,
)

/**
 * ★ keep: データが“空”ならフォルダ/ノートを投入する拡張関数
 * - Notes が1件でもあれば何もしない（重複投入防止）
 * - Folder は名前→ID をマップしつつ必要ぶんだけ追加
 * - Note は upsert(id=0L) で挿入し、必要なら starred を反映
 */
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
        // 0L の可能性あり（実装依存）
        if (id != 0L) {
            nameToId[name] = id
        } else {
            // 0L の場合でも、後続のノートで folderName が参照されても null で挿入されるだけで致命的ではない
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
        // 実装によっては isStarred が upsert で反映されない可能性に備えて二重化
        if (n.starred && insertedId != 0L) {
            setStarred(insertedId, true)
        }
    }
}
