@file:OptIn( // ★ (必要な場合のみ) Material3 の Experimental API を明示許可。不要なら削除可。
    androidx.compose.material3.ExperimentalMaterial3Api::class
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder

// ====== Material3（必要なものだけ明示 import に統一） ======
// import androidx.compose.material3.*                 // ★ Removed: 冗長(ワイルドカード)を廃止
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider     // ★ Added: Divider 後継
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme        // ★ Added: MaterialTheme を明示 import
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar            // ★ Added: SmallTopAppBar ではなく TopAppBar を使用
import androidx.compose.material3.VerticalDivider      // ★ Added: Divider 後継
import androidx.compose.material3.Surface

// ====== Compose ランタイム ======
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// ====== Lifecycle Compose ======
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ====== アプリのモデル ======
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun BugsScreen(
    vm: NotesViewModel = viewModel()
) {
    val notes by vm.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    val query by vm.query.collectAsStateWithLifecycle(initialValue = "")

    var showFolderMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // ★ Changed: SmallTopAppBar → TopAppBar（安定API寄りに）
            TopAppBar(
                title = { Text("Bug Memo") },
                actions = {
                    TextField(
                        value = query,
                        onValueChange = { vm.setQuery(it) }, // ★ setQuery を実際に使用
                        singleLine = true,
                        placeholder = { Text("検索…") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(220.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.newNote() }) { // ★ newNote 使用
                Icon(Icons.Default.Add, contentDescription = "New")
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
                            onClick = { vm.loadNote(note.id) },                    // ★ loadNote 使用
                            onToggleStar = { vm.toggleStar(note.id, note.isStarred) } // ★ toggleStar 使用
                        )
                    }
                }
            }

            // ★ Changed: Divider → VerticalDivider（非推奨APIの置換）
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp
            )

            // 右：エディタ
            EditorPane(
                editing = editing,
                folders = folders,
                onTitleChange = { vm.setEditingTitle(it) },       // ★ 使用
                onContentChange = { vm.setEditingContent(it) },   // ★ 使用
                onFolderPick = { vm.setEditingFolder(it) },       // ★ 使用
                onSave = { vm.saveEditing() },                    // ★ 使用
                onDelete = { vm.deleteEditing() },                // ★ 使用
                onAddFolder = { name -> vm.addFolder(name) },     // ★ 使用
                onDeleteFolder = { id -> vm.deleteFolder(id) },   // ★ 使用
                showFolderMenu = showFolderMenu,                  // ★ 使用
                setShowFolderMenu = { showFolderMenu = it }       // ★ 使用
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
            onValueChange = onTitleChange,     // ★ setEditingTitle を使用
            label = { Text("タイトル") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = editing?.content.orEmpty(),
            onValueChange = onContentChange,   // ★ setEditingContent を使用
            label = { Text("内容") },
            minLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        // 区切り
        HorizontalDivider(thickness = 1.dp)   // ★ Changed: Divider → HorizontalDivider

        // フォルダ選択（メニューを実際に使う）
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
                        onFolderPick(null)      // ★ setEditingFolder(null)
                        setShowFolderMenu(false)
                    }
                )
                folders.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f.name) },
                        onClick = {
                            onFolderPick(f.id) // ★ setEditingFolder(id)
                            setShowFolderMenu(false)
                        },
                        trailingIcon = {
                            IconButton(onClick = { onDeleteFolder(f.id) }) { // ★ deleteFolder を使用
                                Icon(Icons.Default.Delete, contentDescription = "Delete folder")
                            }
                        }
                    )
                }
            }
        }

        // 保存・削除・フォルダ追加
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave, enabled = editing != null) { // ★ saveEditing 使用
                Text("保存")
            }
            OutlinedButton(
                onClick = onDelete,                              // ★ deleteEditing 使用
                enabled = (editing?.id ?: 0L) != 0L
            ) { Text("削除") }

            var newFolder by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newFolder,
                onValueChange = { newFolder = it },
                label = { Text("新規フォルダ名") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newFolder.isNotBlank()) onAddFolder(newFolder.trim()) // ★ addFolder 使用
                    newFolder = ""
                }
            ) { Text("追加") }
        }
    }
}
