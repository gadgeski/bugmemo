package com.example.bugmemo.data

import kotlinx.coroutines.flow.Flow

// ──────────────────────────────────────────────────────────────
// Flow ベースのリポジトリIF（← このファイルは IF だけ持つ）
// ──────────────────────────────────────────────────────────────
interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun observeFolders(): Flow<List<Folder>>

    suspend fun getNote(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun deleteNote(id: Long)

    suspend fun addFolder(name: String): Long
    suspend fun deleteFolder(id: Long)

    suspend fun setStarred(id: Long, starred: Boolean) // 既存のまま
}
