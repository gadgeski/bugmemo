package com.example.bugmemo.data.db

// ★ 整理: 必要な import を個別に。ワイルドカードと個別の重複を解消
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Delete
    suspend fun delete(folder: FolderEntity)
}

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
        """
    )
    fun search(q: String): Flow<List<NoteEntity>>
    // Repository 側で "%$query%" の形にして渡す想定（そのままでOK）

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    // ★ 追加: スター状態だけを更新したい時に便利（部分更新）
    @Query("UPDATE notes SET isStarred = :starred, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStarred(id: Long, starred: Boolean, updatedAt: Long)
}
