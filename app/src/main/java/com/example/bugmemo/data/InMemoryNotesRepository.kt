package com.example.bugmemo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * InMemory 実装（開発・テスト用）
 * - Flow ベースの IF（NotesRepository）に準拠
 * - フォルダは Long ID 管理（Room 実装と整合）
 * - データはプロセス終了で消える（永続化なし）
 */
class InMemoryNotesRepository : NotesRepository {
    private val foldersFlow = MutableStateFlow<List<Folder>>(emptyList())
    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())

    private var nextFolderId = 1L
    private var nextNoteId = 1L

    init {
        // 初期データ
        val seedFolders =
            listOf("Swift", "Kotlin", "Networking", "warning").map { name ->
                Folder(id = nextFolderId++, name = name)
            }
        foldersFlow.value = seedFolders

        fun fid(name: String) = seedFolders.firstOrNull { it.name == name }?.id

        val now = System.currentTimeMillis()
        notesFlow.value =
            buildList {
                add(
                    Note(
                        id = nextNoteId++,
                        title = "プレビューが落ちる",
                        content = "SwiftUIのPreviewでクラッシュ。再現手順…",
                        folderId = fid("Swift"),
                        createdAt = now,
                        updatedAt = now,
                        isStarred = false,
                    ),
                )
                add(
                    Note(
                        id = nextNoteId++,
                        title = "API で 500",
                        content = "/v1/tickets が500。再試行で成功あり",
                        folderId = fid("Networking"),
                        createdAt = now,
                        updatedAt = now,
                        isStarred = false,
                    ),
                )
            }
    }

    // 一覧（更新日時降順）
    override fun observeNotes(): Flow<List<Note>> = notesFlow.map { it.sortedByDescending { n -> n.updatedAt } }

    // 検索（タイトル/本文を部分一致、更新日時降順）
    override fun searchNotes(query: String): Flow<List<Note>> = notesFlow.map { list ->
        val q = query.trim()
        if (q.isEmpty()) {
            list.sortedByDescending { it.updatedAt }
        } else {
            list
                .filter { it.title.contains(q, true) || it.content.contains(q, true) }
                .sortedByDescending { it.updatedAt }
        }
    }

    // フォルダ一覧
    override fun observeFolders(): Flow<List<Folder>> = foldersFlow

    // 単発取得（スナップショットから）
    override suspend fun getNote(id: Long): Note? = notesFlow.value.firstOrNull { it.id == id }

    // 作成/更新（id==0 で新規）
    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            val newId = nextNoteId++
            val newNote = note.copy(id = newId, createdAt = now, updatedAt = now)
            notesFlow.update { it + newNote }
            newId
        } else {
            var resultId = note.id
            notesFlow.update { list ->
                list
                    .map {
                        if (it.id == note.id) {
                            note.copy(createdAt = it.createdAt, updatedAt = now)
                        } else {
                            it
                        }
                    }.also { if (list.none { it.id == note.id }) resultId = 0L }
            }
            resultId
        }
    }

    // 削除
    override suspend fun deleteNote(id: Long) {
        notesFlow.update { it.filterNot { n -> n.id == id } }
    }

    // フォルダ作成（同名は再利用）
    override suspend fun addFolder(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return 0L
        foldersFlow.value.firstOrNull { it.name.equals(trimmed, true) }?.let { return it.id }
        val id = nextFolderId++
        foldersFlow.update { it + Folder(id = id, name = trimmed) }
        return id
    }

    // フォルダ削除（ノートの folderId は null 化）
    override suspend fun deleteFolder(id: Long) {
        foldersFlow.update { it.filterNot { f -> f.id == id } }
        notesFlow.update { list ->
            list.map { n -> if (n.folderId == id) n.copy(folderId = null, updatedAt = System.currentTimeMillis()) else n }
        }
    }

    // ★ Added: スター状態だけを部分更新（Room版の DAO.updateStarred と整合）
    override suspend fun setStarred(
        id: Long,
        starred: Boolean,
    ) {
        val now = System.currentTimeMillis()
        notesFlow.update { list ->
            list.map { n -> if (n.id == id) n.copy(isStarred = starred, updatedAt = now) else n }
        }
    }
}
