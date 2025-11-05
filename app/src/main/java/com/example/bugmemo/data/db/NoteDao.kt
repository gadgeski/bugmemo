// app/src/main/java/com/example/bugmemo/data/db/NoteDao.kt
package com.example.bugmemo.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
// ★ keep: 必要な import のみ個別指定

// ─────────────────────────────────────────────
// FolderDao
// ─────────────────────────────────────────────
@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Suppress("unused") // ★ keep: 将来的なダッシュボード用
    @Query("SELECT COUNT(*) FROM folders")
    fun observeFolderCount(): Flow<Long>

    @Suppress("unused") // ★ keep: 起動時チェック等
    @Query("SELECT COUNT(*) FROM folders")
    suspend fun countFolders(): Long

    @Suppress("unused") // ★ keep: バルク挿入
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(folders: List<FolderEntity>): List<Long>
}

// ─────────────────────────────────────────────
// NoteDao
// ─────────────────────────────────────────────
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE :q OR content LIKE :q
        ORDER BY updatedAt DESC
        """,
    )
    fun search(q: String): Flow<List<NoteEntity>>
    // ★ keep: Repository 側で "%$query%" を付与する前提

    // ─────────── PagingSource 追加（ここから）──────────

    // ★ keep: フォルダ絞り込み付き（正規名）
    @Query(
        """
        SELECT * FROM notes
        WHERE (:folderId IS NULL OR folderId = :folderId)
        ORDER BY updatedAt DESC
        """,
    )
    fun pagingSourceByFolder(folderId: Long?): PagingSource<Int, NoteEntity>

    // ★ Removed: 互換用に残していた別名（未使用のため削除）
    // fun pagingSource(folderId: Long?): PagingSource<Int, NoteEntity>

    // ★ Changed: FTS 検索の PagingSource
    @Query(
        """
        SELECT n.*
        FROM notes AS n
        JOIN notesFts AS fts ON fts.rowid = n.id
        WHERE notesFts MATCH :query
        ORDER BY n.updatedAt DESC
        """,
    )
    fun pagingSourceFts(query: String): PagingSource<Int, NoteEntity>

    // ★ keep: LIKE 検索フォールバック（FTS 未準備時に Repository 側オプションで使用）
    @Suppress("unused") // ★ keep: 使うまで警告抑制（Repository で切替えたら外す）
    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE :q OR content LIKE :q
        ORDER BY updatedAt DESC
        """,
    )
    fun pagingSourceLike(q: String): PagingSource<Int, NoteEntity>

    // ─────────── PagingSource 追加（ここまで）──────────

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Suppress("unused") // ★ keep: バルク挿入
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notes: List<NoteEntity>): List<Long>

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    // ★ keep: スター状態のみ部分更新
    @Query("UPDATE notes SET isStarred = :starred, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStarred(
        id: Long,
        starred: Boolean,
        updatedAt: Long,
    )

    @Suppress("unused") // ★ keep: 一覧の空/非空監視等
    @Query("SELECT COUNT(*) FROM notes")
    fun observeNoteCount(): Flow<Long>

    @Suppress("unused") // ★ keep
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Long

    @Suppress("unused") // ★ keep
    @Query("SELECT COUNT(*) FROM notes WHERE folderId = :folderId")
    suspend fun countNotesInFolder(folderId: Long): Long
}
