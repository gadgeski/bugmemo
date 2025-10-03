// app/src/main/java/com/example/bugmemo/ui/screens/BugsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun BugsScreen(
    vm: NotesViewModel = viewModel(),
    onOpenEditor: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenFolders: () -> Unit = {}
) {
    val notes by vm.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    val filterFolderId by vm.filterFolderId.collectAsStateWithLifecycle(initialValue = null)

    var showFolderMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val label = folders.firstOrNull { it.id == filterFolderId }?.name
                    Text(if (label != null) "Bug Memo — $label" else "Bug Memo")
                },
                actions = {
                    if (filterFolderId != null) {
                        IconButton(onClick = { vm.setFolderFilter(null) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear folder filter")
                        }
                    }
                    IconButton(onClick = onOpenFolders) {
                        Icon(Icons.Filled.Folder, contentDescription = "Folders")
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.newNote(); onOpenEditor() }) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "New")
            }
        }
    ) { inner ->
        Row(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 左：一覧
            Box(Modifier.weight(1f)) {
                if (notes.isEmpty()) {
                    EmptyMessage() // ★ Changed: 引数なしの固定表示に変更
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes, key = { n -> n.id }) {
                            NoteRow(
                                note = it,
                                onClick = {
                                    vm.loadNote(it.id)
                                    onOpenEditor()
                                },
                                onToggleStar = { vm.toggleStar(it.id, it.isStarred) }
                            )
                        }
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp
            )

            // 右：エディタ
            EditorPane(
                editing = editing,
                folders = folders,
                onTitleChange = { text -> vm.setEditingTitle(text) },
                onContentChange = { text -> vm.setEditingContent(text) },
                onFolderPick = { id -> vm.setEditingFolder(id) },
                onSave = { vm.saveEditing() },
                onDelete = { vm.deleteEditing() },
                onAddFolder = { name -> vm.addFolder(name) },
                onDeleteFolder = { id -> vm.deleteFolder(id) },
                showFolderMenu = showFolderMenu,
                setShowFolderMenu = { flag -> showFolderMenu = flag }
            )
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "(無題)" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(Icons.Filled.Star, contentDescription = "Starred")
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Not starred")
                }
            }
        }
    }
}

/* ▼▼ ここから EditorPane（既存） ▼▼ */
@Composable
private fun EditorPane(
    editing: Note?,
    folders: List<Folder>,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onFolderPick: (Long?) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteFolder: (Long) -> Unit,
    showFolderMenu: Boolean,
    setShowFolderMenu: (Boolean) -> Unit
) {
    Column(
        Modifier
            .widthIn(min = 340.dp)
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("編集", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = editing?.title.orEmpty(),
            onValueChange = onTitleChange,
            label = { Text("タイトル") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = editing?.content.orEmpty(),
            onValueChange = onContentChange,
            label = { Text("内容") },
            minLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(thickness = 1.dp)

        // フォルダ選択
        Box {
            OutlinedButton(onClick = { setShowFolderMenu(true) }) {
                val current = folders.firstOrNull { it.id == editing?.folderId }?.name
                Text(current ?: "フォルダ未選択")
            }
            DropdownMenu(
                expanded = showFolderMenu,
                onDismissRequest = { setShowFolderMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("未選択（なし）") },
                    onClick = {
                        onFolderPick(null)
                        setShowFolderMenu(false)
                    }
                )
                folders.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f.name) },
                        onClick = {
                            onFolderPick(f.id)
                            setShowFolderMenu(false)
                        },
                        trailingIcon = {
                            IconButton(onClick = { onDeleteFolder(f.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete folder")
                            }
                        }
                    )
                }
            }
        }

        // 保存・削除・フォルダ追加
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave, enabled = editing != null) { Text("保存") }
            OutlinedButton(
                onClick = onDelete,
                enabled = (editing?.id ?: 0L) != 0L
            ) { Text("削除") }

            var newFolder by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newFolder,
                onValueChange = { text -> newFolder = text },
                label = { Text("新規フォルダ名") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val name = newFolder.trim()
                    if (name.isNotEmpty()) onAddFolder(name)
                    newFolder = ""
                }
            ) { Text("追加") }
        }
    }
}
/* ▲▲ ここまで EditorPane ▲▲ */

/* ▼▼ ここから EmptyMessage（引数なし・固定表示版） ▼▼ */
@Composable
private fun EmptyMessage() { // ★ Added: 引数を廃止し、この画面専用の固定文言に
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
            Text("メモがありません", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "右下の + から作成しましょう",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
/* ▲▲ ここまで EmptyMessage ▲▲ */
