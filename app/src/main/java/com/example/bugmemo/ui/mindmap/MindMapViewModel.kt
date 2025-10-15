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

    // ★ Added: 直近で削除したサブツリーのスナップショット（Undo 用）
    // ★ Added: 単発Undo（1段階）想定。必要なら List<Snapshot> にしてスタック化可能。
    private var lastDeletedSnapshot: List<MindNode> = emptyList()

    // ★ Added: 現在のノード集合から nextId を再計算（復元後の衝突防止）
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

    // ★ keep: 子ノード追加 API（親ID直下に1件追加）
    // ★ keep: 親が存在しない場合は何もしない（安全側）
    fun addChildNode(parentId: Long, title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        val current = _nodes.value
        if (current.none { it.id == parentId }) return
        // 親存在チェック

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

    // ★ Changed: 子を持つ親ごと再帰削除 → スナップショットを保持（Undo 対応）
    fun deleteNode(id: Long) {
        val all = _nodes.value
        val toDelete = collectWithChildren(all, id)

        // ★ Added: 復元のために削除対象サブツリーを保持
        lastDeletedSnapshot = all.filter { it.id in toDelete }
        // 順序は createdAt でなく現状の並びを保持

        // 実削除
        _nodes.value = all.filterNot { n -> n.id in toDelete }

        // ★ Added: 復元時のID衝突を避けるため nextId を調整
        recalcNextIdFromCurrent()
    }

    // ★ Added: 直前削除を復元。成功時 true / 復元対象なし false を返す。
    fun undoDelete(): Boolean {
        if (lastDeletedSnapshot.isEmpty()) return false

        // ★ Added: 復元前に現在集合とID衝突しないことを確認し、衝突があれば nextId を進める
        val currentIds = _nodes.value.map { it.id }.toHashSet()
        if (lastDeletedSnapshot.any { it.id in currentIds }) {
            // 本来は ID 変更などのマージ戦略が必要だが、本アプリでは
            // 「削除後に同じIDが再採番されることはない」運用のため nextId を上げ直すだけで十分
            recalcNextIdFromCurrent()
        }

        // 復元：単純結合（IDは元のまま）。distinct は不要な想定（削除直後に限るため）
        _nodes.value = _nodes.value + lastDeletedSnapshot

        // 復元後はスナップショットをクリア
        lastDeletedSnapshot = emptyList()
        return true
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

    // ★ Added: UI からボタン活性/非活性判断に使える補助
    fun canUndoDelete(): Boolean = lastDeletedSnapshot.isNotEmpty()
}
