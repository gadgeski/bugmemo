// app/src/main/java/com/example/bugmemo/ui/screens/MindMapScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Changed: フェーズ0の実 UI（InMemory CRUD）に差し替え
// ★ Changed: 既存アプリ機能に影響しない“単独画面”として動作

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.mindmap.MindMapViewModel
import com.example.bugmemo.ui.mindmap.MindNode

// ★ Added: StateFlow を Compose で購読するための拡張関数を import（collectAsStateWithLifecycle）

@Composable
fun MindMapScreen(
    onClose: () -> Unit = {},
    vm: MindMapViewModel = viewModel(),
) {
    // ★ Added: ViewModel が公開する nodes を購読（未使用警告の解消＆再描画トリガ）
    val nodes by vm.nodes.collectAsStateWithLifecycle(initialValue = emptyList())

    // ★ Changed: flatList() の再計算を nodes の変化に結びつける（remember(nodes)）
    val flat = remember(nodes) { vm.flatList() }

    var newTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ★ Added: 件数を軽く表示して購読を明示
                    Text("Mind Map（${nodes.size}）")
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ★ Added: ルートノードの追加（最小）
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("新規ノード（ルート）") },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        vm.addRootNode(newTitle)
                        newTitle = ""
                    },
                    enabled = newTitle.isNotBlank(),
                ) { Text("追加") }
            }

            // ★ keep: 簡易ツリー（インデント表示）
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(flat, key = { (n, _) -> n.id }) { (node, depth) ->
                    MindNodeRow(
                        node = node,
                        depth = depth,
                        onRename = { title -> vm.renameNode(node.id, title) },
                        onDelete = { vm.deleteNode(node.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MindNodeRow(
    node: MindNode,
    depth: Int,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var edit by remember { mutableStateOf(false) }
    var title by remember(node.id) { mutableStateOf(node.title) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (edit) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                label = { Text("名称") },
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    onRename(title)
                    edit = false
                },
                enabled = title.isNotBlank(),
            ) { Icon(Icons.Filled.Save, contentDescription = "Save") }
        } else {
            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { edit = true }) { Text("編集") }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
    }
}
