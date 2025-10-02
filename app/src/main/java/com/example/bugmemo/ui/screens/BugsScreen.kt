// app/src/main/java/com/example/bugmemo/ui/screens/BugsScreen.kt
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class // TopAppBar の版差異対策（不要になれば削除OK）
)

package com.example.bugmemo.ui.screens

// ====== 基本UI ======
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// ====== アイコン ======
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search   // ★ Added: 検索アイコン
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder

// ====== Material3 ======
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

// ====== Compose ランタイム ======
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// ====== Lifecycle Compose ======
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ====== モデル ======
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun BugsScreen(
    vm: NotesViewModel = viewModel(),
    onOpenEditor: () -> Unit = {},
    onOpenSearch: () -> Unit = {} // ★ Added: Search 画面へ遷移するコールバック
) {
    val notes by vm.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    var showFolderMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bug Memo") },
                actions = {
                    // ★ Changed: TopBar の TextField 検索は削除し、アイコン遷移に一本化
                    IconButton(onClick = onOpenSearch) { // ★ Added
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.newNote() }) {
                Icon(Icons.Filled.Add, contentDescription = "New")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            onClick = {
                                vm.loadNote(note.id) // 編集対象をロード
                                onOpenEditor()       // Editor へ遷移
                            },
                            onToggleStar = { vm.toggleStar(note.id, note.isStarred) }
                        )
                    }
                }
            }

            // 区切り
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp
            )

            // 右：簡易エディタ（現編集状態のプレビュー/編集）
            EditorPane(
                editing = editing,
                folders = folders,
                onTitleChange = { vm.setEditingTitle(it) },
                onContentChange = { vm.setEditingContent(it) },
                onFolderPick = { vm.setEditingFolder(it) },
                onSave = { vm.saveEditing() },
                onDelete = { vm.deleteEditing() },
                onAddFolder = { name -> vm.addFolder(name) },
                onDeleteFolder = { id -> vm.deleteFolder(id) },
                showFolderMenu = showFolderMenu,
                setShowFolderMenu = { showFolderMenu = it }
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

        Box {
            OutlinedButton(onClick = { setShowFolderMenu(true) }) {
                Text(folders.firstOrNull { it.id == editing?.folderId }?.name ?: "フォルダ未選択")
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave, enabled = editing != null) { Text("保存") }
            OutlinedButton(
                onClick = onDelete,
                enabled = (editing?.id ?: 0L) != 0L
            ) { Text("削除") }
        }
    }
}
