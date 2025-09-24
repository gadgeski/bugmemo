// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/data/NotesRepository.kt
// ----------------------------------------
package com.example.bugmemo.data

interface NotesRepository {
    fun all(): List<Note>
    fun find(id: Long): Note?
    fun create(): Note
    fun update(id: Long, title: String, content: String, folder: String?)
    fun delete(id: Long)
    fun folders(): List<String>
    fun addFolder(name: String)
}

class InMemoryNotesRepository : NotesRepository {
    private val _notes = mutableListOf<Note>()
    private val _folders = mutableListOf("Inbox", "UI", "Networking", "Crash")
    private var nextId = 1L

    init {
        if (_notes.isEmpty()) {
            _notes += Note(nextId++, "プレビューが落ちる", "SwiftUIのPreviewでクラッシュ。再現手順…", folder = "UI")
            _notes += Note(nextId++, "API で 500", "/v1/tickets が500。再試行で成功あり", folder = "Networking")
        }
    }

    override fun all(): List<Note> = _notes.sortedByDescending { it.updatedAt }
    override fun find(id: Long): Note? = _notes.firstOrNull { it.id == id }
    override fun create(): Note { val n = Note(nextId++, "", "", null); _notes += n; return n }
    override fun update(id: Long, title: String, content: String, folder: String?) {
        val n = find(id) ?: return
        n.title = title; n.content = content; n.folder = folder; n.updatedAt = System.currentTimeMillis()
    }
    override fun delete(id: Long) { _notes.removeIf { it.id == id } }
    override fun folders(): List<String> = _folders.toList()
    override fun addFolder(name: String) { if (name.isNotBlank() && name !in _folders) _folders += name }
}
