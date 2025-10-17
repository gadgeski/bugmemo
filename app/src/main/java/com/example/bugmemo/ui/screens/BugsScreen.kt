// app/src/main/java/com/example/bugmemo/ui/screens/BugsScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Changed: import を辞書順に並べ替え（途中にコメントを挟まない：ktlint対応）
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.core.FeatureFlags
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

// ★ Added: 文字列リソース参照(androidx.compose.ui.res.stringResource)
// ★ Added: R を参照(com.example.bugmemo.R)
// ★ Added: スクロール用の状態を追加(androidx.compose.foundation.rememberScrollState)
// ★ Added: 縦スクロール修飾子を追加(androidx.compose.foundation.verticalScroll)
// ★ Removed: BuildConfig 直接参照は不要
// import com.example.bugmemo.BuildConfig
// ★ Removed: デフォルト生成をやめたため不要
// import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BugsScreen(
    vm: NotesViewModel,
    // ★ keep: 必須受け取り（重複VM防止）
    onOpenEditor: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenFolders: () -> Unit = {},
    onOpenMindMap: () -> Unit = {},
    // ★ keep: MindMap への導線（Nav から渡す）
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
                    // ★ Changed: タイトルをリソース化（フォルダ選択時は "Bug Memo — %s"）
                    Text(
                        text = if (label != null) {
                            stringResource(R.string.title_bugmemo_with_label, label)
                        } else {
                            stringResource(R.string.app_name)
                        },
                    )
                },
                actions = {
                    if (filterFolderId != null) {
                        IconButton(onClick = { vm.setFolderFilter(null) }) {
                            // ★ Changed: CD をリソース化（クリア→delete の文言を流用）
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_delete))
                        }
                    }
                    IconButton(onClick = onOpenFolders) {
                        // ★ Changed: CD をリソース化
                        Icon(Icons.Filled.Folder, contentDescription = stringResource(R.string.cd_open_folders))
                    }
                    IconButton(onClick = onOpenSearch) {
                        // ★ Changed: CD をリソース化
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_open_search))
                    }
                    // ★ Changed: BuildConfig.DEBUG → FeatureFlags.ENABLE_MIND_MAP_DEBUG
                    if (FeatureFlags.ENABLE_MIND_MAP_DEBUG) {
                        // ★ keep: 開発時のみ Mind Map(Dev) へのショートカットを表示（UI非破壊）
                        IconButton(onClick = onOpenMindMap) {
                            // ★ Changed: CD をリソース化
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.cd_open_mindmap_dev),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vm.newNote()
                    onOpenEditor()
                },
            ) {
                // ★ Changed: CD をリソース化
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.cd_new_note))
            }
        },
    ) { inner ->
        Row(
            Modifier
                .padding(inner)
                .fillMaxSize(),
        ) {
            // 左：一覧
            Box(Modifier.weight(1f)) {
                if (notes.isEmpty()) {
                    // ★ keep: 固定文言の空状態表示
                    EmptyMessage()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(notes, key = { n -> n.id }) {
                            NoteRow(
                                note = it,
                                onClick = {
                                    vm.loadNote(it.id)
                                    onOpenEditor()
                                },
                                onToggleStar = { vm.toggleStar(it.id, it.isStarred) },
                            )
                        }
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
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
                setShowFolderMenu = { flag -> showFolderMenu = flag },
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
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "(無題)" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(Icons.Filled.Star, contentDescription = null)
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = null)
                }
            }
        }
    }
}

/* ▼▼ ここから EditorPane（スクロール付与版） ▼▼ */
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
    setShowFolderMenu: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .widthIn(min = 340.dp)
            .fillMaxHeight()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        // ★ keep: 編集ペイン全体を縦スクロール可能に
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("編集", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = editing?.title.orEmpty(),
            onValueChange = onTitleChange,
            label = { Text("タイトル") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = editing?.content.orEmpty(),
            onValueChange = onContentChange,
            label = { Text("内容") },
            minLines = 6,
            modifier = Modifier.fillMaxWidth(),
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
                onDismissRequest = { setShowFolderMenu(false) },
            ) {
                DropdownMenuItem(
                    text = { Text("未選択（なし）") },
                    onClick = {
                        onFolderPick(null)
                        setShowFolderMenu(false)
                    },
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
                        },
                    )
                }
            }
        }

        // 保存・削除・フォルダ追加
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave, enabled = editing != null) { Text("保存") }
            OutlinedButton(
                onClick = onDelete,
                enabled = (editing?.id ?: 0L) != 0L,
            ) { Text("削除") }

            var newFolder by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newFolder,
                onValueChange = { text -> newFolder = text },
                label = { Text("新規フォルダ名") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    val name = newFolder.trim()
                    if (name.isNotEmpty()) onAddFolder(name)
                    newFolder = ""
                },
            ) { Text("追加") }
        }
    }
}
/* ▲▲ ここまで EditorPane ▲▲ */

/* ▼▼ ここから EmptyMessage（引数なし・固定表示版） ▼▼ */
@Composable
private fun EmptyMessage() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        tonalElevation = 0.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // ★ Changed: ハードコード → 文字列リソース
            Text(
                text = stringResource(R.string.empty_no_memo),
                style = MaterialTheme.typography.titleMedium,
            )
            // ★ Changed: ハードコード → 文字列リソース
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_tip_create),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
/* ▲▲ ここまで EmptyMessage ▲▲ */
