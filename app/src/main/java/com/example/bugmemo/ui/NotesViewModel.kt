// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/NotesViewModel.kt
// ----------------------------------------
package com.example.bugmemo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bugmemo.data.InMemoryNotesRepository
import com.example.bugmemo.data.Note
import com.example.bugmemo.data.NotesRepository

class NotesViewModel(
    private val repo: NotesRepository = InMemoryNotesRepository()
) : ViewModel() {
    // Expose UI state as Compose-friendly structures
    private val _notes = mutableStateListOf<Note>().apply { addAll(repo.all()) }
    val notes: List<Note> get() = _notes
    val notesSorted: List<Note> get() = _notes.sortedByDescending { it.updatedAt }

    private var _foldersState = mutableStateListOf<String>().apply { addAll(repo.folders()) }
    val folders: List<String> get() = _foldersState

    var searchResults by mutableStateOf<List<Note>>(emptyList())
        private set

    fun createNote() {
        repo.create(); refresh()
    }

    fun updateNote(id: Long, title: String, content: String, folder: String?) {
        repo.update(id, title, content, folder); refresh()
    }

    fun deleteNote(id: Long) { repo.delete(id); refresh() }

    fun findById(id: Long): Note? = repo.find(id)

    fun addFolder(name: String) { repo.addFolder(name); _foldersState.clear(); _foldersState.addAll(repo.folders()) }

    fun search(q: String): List<Note> {
        val query = q.trim()
        val res = if (query.isBlank()) emptyList() else notes.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }.sortedByDescending { it.updatedAt }
        searchResults = res
        return res
    }

    private fun refresh() {
        _notes.clear(); _notes.addAll(repo.all())
    }
}
