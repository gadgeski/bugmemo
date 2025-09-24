// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/data/Models.kt
// ----------------------------------------
package com.example.bugmemo.data

data class Note(
    val id: Long,
    var title: String,
    var content: String,
    var folder: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
