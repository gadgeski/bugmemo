// app/src/main/java/com/example/bugmemo/ui/screens/FoldersScreen.kt
// FoldersScreen.kt の先頭行に追加
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.data.Folder
import com.example.bugmemo.ui.NotesViewModel
import kotlinx.coroutines.launch

/**
 * フォルダ一覧画面（完成版）
 * - 行タップ：フォルダ絞り込みの切替
 * - 「＋」：そのフォルダで新規ノート作成 → onOpenEditor() で編集画面へ
 * - 上部フォーム：フォルダ追加
 */
@Composable
fun FoldersScreen(
    vm: NotesViewModel = viewModel(),
    onOpenEditor: () -> Unit = {}  // Nav から受け取るエディタ遷移のコールバック
) {
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentFilter by vm.filterFolderId.collectAsStateWithLifecycle(initialValue = null)

    var newFolder by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope() // ★ Added: suspend API を呼ぶためのコルーチン

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
                actions = {
                    if (currentFilter != null) {
                        IconButton(onClick = { vm.setFolderFilter(null) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear filter")
                        }
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 新規フォルダ追加
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newFolder,
                    onValueChange = { newFolder = it },
                    label = { Text("新規フォルダ名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val name = newFolder.trim()
                        if (name.isNotEmpty()) {
                            // ★ Changed: onClick 内は Composable ではないので、コルーチンで suspend を呼ぶ
                            scope.launch {
                                vm.addFolder(name)
                                newFolder = ""
                            }
                        }
                    }
                ) { Text("追加") }
            }

            if (folders.isEmpty()) {
                EmptyFoldersMessage()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(folders, key = { it.id }) { folder ->
                        FolderRow(
                            folder = folder,
                            isActive = (folder.id == currentFilter),
                            onSetFilter = { id -> vm.setFolderFilter(id) },
                            onDelete = { id ->
                                scope.launch { vm.deleteFolder(id) } // ★ suspend を安全に呼ぶ
                            },
                            onCreateNoteHere = { id ->
                                vm.newNote()
                                vm.setEditingFolder(id)
                                onOpenEditor() // ★ Added: エディタへ遷移
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderRow(
    folder: Folder,
    isActive: Boolean,
    onSetFilter: (Long?) -> Unit,
    onDelete: (Long) -> Unit,
    onCreateNoteHere: (Long) -> Unit
) {
    Surface(
        tonalElevation = if (isActive) 4.dp else 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .clickable { onSetFilter(if (isActive) null else folder.id) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Folder, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Text(
                        text = "（絞り込み中）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = { onCreateNoteHere(folder.id) }) {
                Icon(Icons.Filled.Add, contentDescription = "Create note here")
            }
            IconButton(onClick = { onDelete(folder.id) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete folder")
            }
        }
    }
}

@Composable
private fun EmptyFoldersMessage() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("フォルダがありません", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "上の入力欄から追加できます",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
