// app/src/main/java/com/example/bugmemo/data/db/FolderDao.kt
package com.example.bugmemo.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bugmemo.data.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY name")
    fun observeFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder): Long

    @Delete
    suspend fun delete(folder: Folder)

    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM folders")
    fun observeFolderCount(): Flow<Long>

    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM folders")
    suspend fun countFolders(): Long

    @Suppress("unused")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(folders: List<Folder>): List<Long>
}
