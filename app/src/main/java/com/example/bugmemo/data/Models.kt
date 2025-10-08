package com.example.bugmemo.data

/** ドメインモデル（アプリ内での真実の型定義） */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,

    // Entity と揃える（未分類は null）
    val folderId: Long?,

    val createdAt: Long,
    val updatedAt: Long,

    // v2 で追加（既定: 未スター）
    val isStarred: Boolean = false,
)

data class Folder(
    val id: Long = 0L,
    val name: String,
)
