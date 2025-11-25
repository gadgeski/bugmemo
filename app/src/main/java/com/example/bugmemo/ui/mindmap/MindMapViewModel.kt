// app/src/main/java/com/example/bugmemo/ui/mindmap/MindMapViewModel.kt
package com.example.bugmemo.ui.mindmap

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MindNode(
    val id: Long,
    val title: String,
    val parentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * MindMapViewModel (Hilt Edition)
 * - Hilt 対応させることで NavHost から hiltViewModel() で取得可能にする
 * - 現状は依存関係なしだが、@Inject constructor() を明示する
 */
@HiltViewModel
class MindMapViewModel @Inject constructor() : ViewModel() {

    // ★ keep: InMemory のノード一覧
    private val _nodes = MutableStateFlow<List<MindNode>>(emptyList())

    // ★ keep: UI から購読する StateFlow
    val nodes: StateFlow<List<MindNode>> = _nodes

    // ★ keep: ID 採番（簡易）
    private var nextId = 1L

    // ★ keep: 直近で削除したサブツリーのスナップショット（Undo 用）
    private var lastDeletedSnapshot: List<MindNode> = emptyList()

    // ★ keep: 現在のノード集合から nextId を再計算
    private fun recalcNextIdFromCurrent() {
        val maxId = _nodes.value.maxOfOrNull { it.id } ?: 0L
        if (nextId <= maxId) nextId = maxId + 1
    }

    // ★ keep: ルートにノードを追加
    fun addRootNode(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        val now = System.currentTimeMillis()
        val node = MindNode(id = nextId++, title = t, parentId = null, createdAt = now, updatedAt = now)
        _nodes.update { it + node }
    }

    // ★ keep: 子ノード追加
    fun addChildNode(parentId: Long, title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        val current = _nodes.value
        if (current.none { it.id == parentId }) return

        val now = System.currentTimeMillis()
        val node = MindNode(id = nextId++, title = t, parentId = parentId, createdAt = now, updatedAt = now)
        _nodes.update { it + node }
    }

    // ★ keep: タイトル編集
    fun renameNode(id: Long, newTitle: String) {
        val t = newTitle.trim()
        if (t.isEmpty()) return
        val now = System.currentTimeMillis()
        _nodes.update { list ->
            list.map { n -> if (n.id == id) n.copy(title = t, updatedAt = now) else n }
        }
    }

    // ★ keep: 削除
    fun deleteNode(id: Long) {
        val all = _nodes.value
        val toDelete = collectWithChildren(all, id)

        lastDeletedSnapshot = all.filter { it.id in toDelete }
        _nodes.value = all.filterNot { n -> n.id in toDelete }
        recalcNextIdFromCurrent()
    }

    // ★ keep: Undo
    fun undoDelete(): Boolean {
        if (lastDeletedSnapshot.isEmpty()) return false

        val currentIds = _nodes.value.map { it.id }.toHashSet()
        if (lastDeletedSnapshot.any { it.id in currentIds }) {
            recalcNextIdFromCurrent()
        }

        _nodes.value = _nodes.value + lastDeletedSnapshot
        lastDeletedSnapshot = emptyList()
        return true
    }

    // ★ keep: フラットリスト変換
    fun flatList(): List<Pair<MindNode, Int>> {
        val children = _nodes.value.groupBy { it.parentId }
        val result = mutableListOf<Pair<MindNode, Int>>()
        fun dfs(parentId: Long?, depth: Int) {
            val list = children[parentId].orEmpty().sortedBy { it.createdAt }
            list.forEach { n ->
                result += n to depth
                dfs(n.id, depth + 1)
            }
        }
        dfs(null, 0)
        return result
    }

    // ★ keep: 子孫収集ヘルパー
    private fun collectWithChildren(all: List<MindNode>, rootId: Long): Set<Long> {
        val children = all.groupBy { it.parentId }
        val bag = linkedSetOf<Long>()
        fun rec(id: Long) {
            bag += id
            children[id].orEmpty().forEach { rec(it.id) }
        }
        rec(rootId)
        return bag
    }

    fun canUndoDelete(): Boolean = lastDeletedSnapshot.isNotEmpty()
}
