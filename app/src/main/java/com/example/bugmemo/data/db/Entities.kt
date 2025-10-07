package com.example.bugmemo.data.db

// 必要最小の import のみ（ワイルドカード・重複なし）
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true) val name: String
)

@Entity(
    tableName = "notes",
    indices = [Index("folderId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,

    // 未分類は null（外部キー未設定）
    val folderId: Long?,

    val createdAt: Long,
    val updatedAt: Long,

    // v2 で追加した列。DB では 0/1、Kotlin 側は Boolean。
    @ColumnInfo(name = "isStarred", defaultValue = "0")
    val isStarred: Boolean = false
)
