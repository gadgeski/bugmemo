package com.example.bugmemo.data.db

// ★ 整理: 必要な import を個別に。ワイルドカードと個別の重複を解消
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
// ★ Added: バルク挿入で ON CONFLICT を指定するため(androidx.room.OnConflictStrategy)

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY name")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Delete
    suspend fun delete(folder: FolderEntity)

    // ★ Added: 使うまで警告を抑制（ダッシュボード等で件数を監視予定）
    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM folders")
    fun observeFolderCount(): Flow<Long>

    // ★ Added: 使うまで警告を抑制（起動時チェック等で単発取得予定）
    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM folders")
    suspend fun countFolders(): Long

    // ★ Added: 使うまで警告を抑制（デバッグシード/インポートで利用予定）
    @Suppress("unused")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(folders: List<FolderEntity>): List<Long>
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
        """,
    )
    fun search(q: String): Flow<List<NoteEntity>>
    // Repository 側で "%$query%" の形にして渡す想定（そのままでOK）

    @Insert
    suspend fun insert(note: NoteEntity): Long

    // ★ Added: 使うまで警告を抑制（デバッグシード/一括インポートで利用予定）
    @Suppress("unused")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notes: List<NoteEntity>): List<Long>

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    // ★ 追加: スター状態だけを更新したい時に便利（部分更新）
    @Query("UPDATE notes SET isStarred = :starred, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStarred(
        id: Long,
        starred: Boolean,
        updatedAt: Long,
    )

    // ★ Added: 使うまで警告を抑制（一覧の空/非空UI切替のリアルタイム反映で利用予定）
    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM notes")
    fun observeNoteCount(): Flow<Long>

    // ★ Added: 使うまで警告を抑制（単発の総数チェックで利用予定）
    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Long

    // ★ Added: 使うまで警告を抑制（フォルダ別バッジや集計で利用予定）
    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM notes WHERE folderId = :folderId")
    suspend fun countNotesInFolder(folderId: Long): Long
}
