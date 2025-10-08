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
