// app/src/main/java/com/example/bugmemo/data/RoomNotesRepository.kt
package com.example.bugmemo.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.bugmemo.data.db.FolderDao
import com.example.bugmemo.data.db.FolderEntity
import com.example.bugmemo.data.db.NoteDao
import com.example.bugmemo.data.db.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
// ★ keep: 必要な import のみ

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

    // ★ keep: 部分更新
    override suspend fun setStarred(id: Long, starred: Boolean) {
        val now = System.currentTimeMillis()
        notes.updateStarred(id = id, starred = starred, updatedAt = now)
    }

    // ─────────── 件数・バルク系（パススルー） ───────────
    override fun observeNoteCount(): Flow<Long> = notes.observeNoteCount()
    override fun observeFolderCount(): Flow<Long> = folders.observeFolderCount()
    override suspend fun countNotes(): Long = notes.countNotes()
    override suspend fun countFolders(): Long = folders.countFolders()
    override suspend fun countNotesInFolder(folderId: Long): Long = notes.countNotesInFolder(folderId)

    override suspend fun insertAllNotes(notes: List<Note>): List<Long> {
        val now = System.currentTimeMillis()
        val entities = notes.map { n ->
            NoteEntity(
                id = n.id, // 0L なら autoGenerate 想定
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

    // ─────────── Paging 3（Pager）実装 ───────────

    override fun pagedNotesByFolder(
        folderId: Long?,
        pageSize: Int,
    ): Flow<PagingData<Note>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize / 2,
        ),
        pagingSourceFactory = {
            notes.pagingSourceByFolder(folderId)
            // ★ Changed: DAO の正規名を使用（互換の pagingSource(...) は削除済み想定）
        },
    ).flow.map { paging -> paging.map(::toDomain) }

    override fun pagedNotes(pageSize: Int): Flow<PagingData<Note>> = pagedNotesByFolder(folderId = null, pageSize = pageSize)
    // ★ Fixed: ここで未定義の folderId を渡さない（null を明示）

    override fun pagedSearch(query: String, pageSize: Int): Flow<PagingData<Note>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize / 2,
        ),
        pagingSourceFactory = {
            notes.pagingSourceFts(query)
            // ★ ensure-use: FTS を使うことで NoteDao の "pagingSourceFts is never used" を解消
            // ★ FTS 未準備なら下の LIKE 版に切替え、DAO の FTS 関数に @Suppress("unused") を付与
            // return@pagingSourceFactory notes.pagingSourceLike("%$query%")
        },
    ).flow.map { paging -> paging.map(::toDomain) }

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
