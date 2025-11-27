// app/src/main/java/com/example/bugmemo/data/db/MindMapEntity.kt
package com.example.bugmemo.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mind_map_nodes")
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "parent_id") val parentId: Long?,
    // ★ Added: 連携するノートのID（Nullなら連携なし）
    @ColumnInfo(name = "note_id") val noteId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
