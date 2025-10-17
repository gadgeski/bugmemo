// app/src/main/java/com/example/bugmemo/ui/screens/MindMapScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ keep: フェーズ0→1へ。既存機能に影響しない“単独画面”として動作
// ★ keep: フォーカス/IME対応・Snackbar用のimport
// ★ Added: キーボード表示時の下部被り回避に使用(androidx.compose.foundation.layout.imePadding)

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.mindmap.MindMapViewModel
import com.example.bugmemo.ui.mindmap.MindNode
import kotlinx.coroutines.launch

// ★ keep: StateFlow を Compose で購読するための拡張関数を使用（collectAsStateWithLifecycle）
// ★ keep: Undo アクション結果の判定に使用(androidx.compose.material3.SnackbarResult)

@Composable
fun MindMapScreen(
    onClose: () -> Unit = {},
    vm: MindMapViewModel = viewModel(),
) {
    // ★ keep: ViewModel が公開する nodes(StateFlow) を購読（再描画トリガ）
    val nodes by vm.nodes.collectAsStateWithLifecycle(initialValue = emptyList())

    // ★ keep: flatList() の再計算を nodes の変化に結びつける（remember(nodes) でメモ化）
    val flat = remember(nodes) { vm.flatList() }

    var newTitle by remember { mutableStateOf("") }

    // ★ keep: Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    // ★ keep: 件数を簡易表示して購読を明示
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
                .padding(16.dp)
                .imePadding(),
            // ★ Added: IME(ソフトキーボード)表示時に下部が隠れないよう余白を付与
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ★ keep: ルートノードの追加（最小 UI）
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("新規ノード（ルート）") },
                    modifier = Modifier.weight(1f),
                    // ★ keep: Enterで確定できるよう IME アクションを設定
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val t = newTitle.trim()
                            if (t.isNotEmpty()) {
                                vm.addRootNode(t)
                                newTitle = ""
                                scope.launch { snackbarHostState.showSnackbar("ルートに追加しました") }
                            }
                        },
                    ),
                )
                Button(
                    onClick = {
                        vm.addRootNode(newTitle)
                        newTitle = ""
                        scope.launch { snackbarHostState.showSnackbar("ルートに追加しました") }
                    },
                    enabled = newTitle.isNotBlank(),
                ) { Text("追加") }
            }

            // ★ keep: 簡易ツリー（インデント表示；(node, depth) を表示）
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(flat, key = { (n, _) -> n.id }) { (node, depth) ->
                    MindNodeRow(
                        node = node,
                        depth = depth,
                        onRename = { title ->
                            vm.renameNode(node.id, title)
                            scope.launch { snackbarHostState.showSnackbar("名称を更新しました") }
                        },
                        onDelete = {
                            // ★ keep: 削除 → Snackbar で「取り消す」アクションを提示し、押下で vm.undoDelete() を呼ぶ
                            vm.deleteNode(node.id)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "削除しました",
                                    actionLabel = if (vm.canUndoDelete()) "取り消す" else null,
                                    withDismissAction = true,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    vm.undoDelete()
                                }
                            }
                        },
                        onAddChild = { title ->
                            vm.addChildNode(node.id, title)
                            scope.launch { snackbarHostState.showSnackbar("子ノードを追加しました") }
                        },
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
    // ★ keep: 子ノード追加用コールバック
    onAddChild: (String) -> Unit,
) {
    var edit by remember { mutableStateOf(false) }
    var title by remember(node.id) { mutableStateOf(node.title) }

    // ★ keep: 子追加用の編集状態と入力値（行内に最小表示）
    var addingChild by remember { mutableStateOf(false) }
    var childTitle by remember { mutableStateOf("") }

    // ★ keep: 子追加行の自動フォーカス用
    val childFocusRequester = remember { FocusRequester() }
    LaunchedEffect(addingChild) {
        // トグルで表示された直後にフォーカスを当てる
        if (addingChild) childFocusRequester.requestFocus()
    }

    Column(
        // ★ keep: 一行構成 → Column 化して下に子追加 UI をぶら下げる
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (edit) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("名称") },
                    modifier = Modifier.weight(1f),
                    // ★ keep: Enterで名称確定できるようIMEアクション
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val t = title.trim()
                            if (t.isNotEmpty()) {
                                onRename(t)
                                edit = false
                            }
                        },
                    ),
                )
                IconButton(
                    onClick = {
                        onRename(title.trim())
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
                // ★ keep: 子追加トグル（開いたら入力→自動フォーカス）
                Button(onClick = { addingChild = !addingChild }) {
                    Text(if (addingChild) "子追加を閉じる" else "子追加")
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        }

        // ★ keep: 子追加の入力行（トグルで表示）
        if (addingChild) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = childTitle,
                    onValueChange = { childTitle = it },
                    singleLine = true,
                    label = { Text("子ノード名") },
                    modifier = Modifier
                        .weight(1f)
                        // ★ keep: トグル直後に自動フォーカス
                        .focusRequester(childFocusRequester),
                    // ★ keep: Enterで確定→即クローズ
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val t = childTitle.trim()
                            if (t.isNotEmpty()) {
                                onAddChild(t)
                                childTitle = ""
                                addingChild = false
                            }
                        },
                    ),
                )
                Button(
                    onClick = {
                        val t = childTitle.trim()
                        if (t.isNotEmpty()) {
                            onAddChild(t)
                            childTitle = ""
                            addingChild = false
                        }
                    },
                    enabled = childTitle.isNotBlank(),
                ) { Text("追加") }
            }
        }
    }
}
