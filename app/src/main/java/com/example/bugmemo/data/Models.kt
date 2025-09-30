package com.example.bugmemo.data

// ★ ドメインの真実の場所：ここ以外で Note/Folder を宣言しない
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val folderId: Long?,              // ★ Entity と揃える
    val createdAt: Long,
    val updatedAt: Long,
    val isStarred: Boolean = false    // ★ v2 で追加
)

data class Folder(
    val id: Long = 0L,
    val name: String
)
