package com.example.bugmemo.data.db

// import は必要なものだけに整理（重複/ワイルドカードを排除） // ★ 整理
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
    val folderId: Long?,            // null = 未分類
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(name = "isStarred", defaultValue = "0") // ★ 追加: v2で追加した列（DBは0/1, KotlinはBoolean）
    val isStarred: Boolean = false                      // ★ 追加: デフォルトは false
)
