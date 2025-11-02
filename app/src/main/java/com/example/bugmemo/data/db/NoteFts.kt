// app/src/main/java/com/example/bugmemo/data/db/NoteFts.kt
package com.example.bugmemo.data.db

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

// ★ FTS は Fts3/Fts4 を使用（Room に Fts5 は無い/androidx.room.Entity）
// ★ UNICODE61 を使うため/androidx.room.FtsOptions
// ★ Fixed: @Fts5 → @Fts4 に変更
// ★ Added: tokenizer = UNICODE61 を指定（日本語・記号の扱いを改善）
// ★ contentEntity=NoteEntity で外部コンテンツ方式（トリガは Room が自動生成）
@Fts4(
    contentEntity = NoteEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
)
@Entity(tableName = "notesFts") // ★ 統一: テーブル名は notesFts に統一
data class NoteFts(
    val title: String,
    val content: String,
)
