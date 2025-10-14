// app/src/main/java/com/example/bugmemo/ui/mindmap/MindMapViewModel.kt
package com.example.bugmemo.ui.mindmap

// ★ keep: フェーズ0の最小実装（InMemory で CRUD だけ）
// ★ keep: 今は Room 連携なし。ノードはプロセス終了で消えます。
// ★ keep: CI 影響を避けるため、既存コードには依存しない独立 ViewModel にしています。

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MindNode(
    val id: Long,
    val title: String,
    val parentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

class MindMapViewModel : ViewModel() {

    // ★ keep: InMemory のノード一覧
    private val _nodes = MutableStateFlow<List<MindNode>>(emptyList())

    // ★ keep: UI から購読する StateFlow
    val nodes: StateFlow<List<MindNode>> = _nodes

    // ★ keep: ID 採番（簡易）
    private var nextId = 1L

    // ★ keep: ルートにノードを追加
    fun addRootNode(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        val now = System.currentTimeMillis()
        val node = MindNode(id = nextId++, title = t, parentId = null, createdAt = now, updatedAt = now)
        _nodes.update { it + node }
    }

    // ★ Added: 子ノード追加 API（親ID直下に1件追加）
    // ★ Added: 親が存在しない場合は何もしない（安全側）
    fun addChildNode(parentId: Long, title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        val current = _nodes.value
        if (current.none { it.id == parentId }) return // ★ Added: 親存在チェック

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

    // ★ keep: 子を持つ親ごと再帰削除（最小）
    fun deleteNode(id: Long) {
        val all = _nodes.value
        val toDelete = collectWithChildren(all, id)
        _nodes.value = all.filterNot { n -> n.id in toDelete }
    }

    // ★ keep: 階層構造をインデント付きフラットリストに変換（簡易ツリー表示用）
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

    // ★ keep: ヘルパー（id とその子孫を集める）
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
}
