package com.example.bugmemo.data

// 必要なものだけ個別 import（ワイルドカードは避ける）
import com.example.bugmemo.data.db.FolderDao
import com.example.bugmemo.data.db.FolderEntity
import com.example.bugmemo.data.db.NoteDao
import com.example.bugmemo.data.db.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * NOTE:
 * - このファイルでは Note/Folder/NotesRepository を再宣言しない（重複回避）。
 *   それらは data/Models.kt・data/NotesRepository.kt が“真実の場所”。
 */
class RoomNotesRepository(
    private val notes: NoteDao,
    private val folders: FolderDao,
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> = notes.observeNotes().map { list -> list.map(::toDomain) }

    override fun searchNotes(query: String): Flow<List<Note>> = notes.search("%$query%").map { list -> list.map(::toDomain) }

    override fun observeFolders(): Flow<List<Folder>> = folders.observeFolders().map { list -> list.map { Folder(it.id, it.name) } }

    override suspend fun getNote(id: Long): Note? = notes.getById(id)?.let(::toDomain)

    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            notes.insert(
                NoteEntity(
                    title = note.title,
                    content = note.content,
                    folderId = note.folderId,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = note.isStarred,
                ),
            )
        } else {
            notes.update(
                NoteEntity(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folderId,
                    createdAt = note.createdAt,
                    updatedAt = now,
                    isStarred = note.isStarred,
                ),
            )
            note.id
        }
    }

    override suspend fun deleteNote(id: Long) {
        notes.getById(id)?.let { notes.delete(it) }
    }

    override suspend fun addFolder(name: String): Long = folders.insert(FolderEntity(name = name))

    override suspend fun deleteFolder(id: Long) {
        folders.delete(FolderEntity(id = id, name = ""))
    }

    // ★ Added: NotesRepository に追加した API を実装（DAO の部分更新を使用）
    override suspend fun setStarred(
        id: Long,
        starred: Boolean,
    ) {
        val now = System.currentTimeMillis()
        notes.updateStarred(id = id, starred = starred, updatedAt = now)
    }

    // ─────────── ここから “未使用警告” 解消のためのパススルー実装 ───────────

    // ★ Added: 件数の監視／単発取得（UI のバッジや空表示切替で使用）
    override fun observeNoteCount(): Flow<Long> = notes.observeNoteCount()
    override fun observeFolderCount(): Flow<Long> = folders.observeFolderCount()
    override suspend fun countNotes(): Long = notes.countNotes()
    override suspend fun countFolders(): Long = folders.countFolders()
    override suspend fun countNotesInFolder(folderId: Long): Long = notes.countNotesInFolder(folderId)

    // ★ Added: バルク挿入（デバッグシード／インポート機能などで使用）
    override suspend fun insertAllNotes(notes: List<Note>): List<Long> {
        val now = System.currentTimeMillis()
        val entities = notes.map { n ->
            // createdAt/updatedAt が 0 の場合は now を充当して挿入の一貫性を確保
            NoteEntity(
                id = n.id,
                // 0L なら autoGenerate 側に委ねる想定
                title = n.title,
                content = n.content,
                folderId = n.folderId,
                createdAt = if (n.createdAt == 0L) now else n.createdAt,
                updatedAt = if (n.updatedAt == 0L) now else n.updatedAt,
                isStarred = n.isStarred,
            )
        }
        return this.notes.insertAll(entities)
    }

    override suspend fun insertAllFolders(folders: List<Folder>): List<Long> {
        val entities = folders.map { f -> FolderEntity(id = f.id, name = f.name) }
        return this.folders.insertAll(entities)
    }

    // ─────────── ヘルパ ───────────
    private fun toDomain(e: NoteEntity) = Note(
        id = e.id,
        title = e.title,
        content = e.content,
        folderId = e.folderId,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
        isStarred = e.isStarred,
    )
}
