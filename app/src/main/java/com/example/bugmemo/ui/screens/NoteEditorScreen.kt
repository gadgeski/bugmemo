// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
package com.example.bugmemo.ui.screens

// ★ Removed: エイリアス import はやめる
// import androidx.compose.material.icons.automirrored.filled.ArrowBack as AutoMirroredArrowBack
// ★ Added: 通常 import にして、本体では Icons.AutoMirrored... で参照
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.bugmemo.ui.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    vm: NotesViewModel,
    onClose: () -> Unit = {}
) {
    val editing = vm.editing.collectAsStateWithLifecycle(initialValue = null).value
    // ★ 型推論で OK（明示型引数は不要）
    val folders = vm.folders.collectAsStateWithLifecycle(initialValue = emptyList()).value

    var showFolderMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(editing?.title?.ifBlank { "(新規メモ)" } ?: "(新規メモ)") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        // ★ Changed: Icons.AutoMirrored.Filled.ArrowBack を直接使用
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val id = editing?.id ?: 0L
                            if (id != 0L) vm.deleteEditing()
                            onClose()
                        },
                        enabled = (editing?.id ?: 0L) != 0L
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                onValueChange = { vm.setEditingTitle(it) },
                label = { Text("タイトル") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { vm.setEditingContent(it) },
                label = { Text("内容") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // フォルダ選択
            Box {
                OutlinedButton(onClick = { showFolderMenu = true }) {
                    val name = folders.firstOrNull { it.id == editing?.folderId }?.name ?: "フォルダ未選択"
                    Text(name)
                }
                DropdownMenu(
                    expanded = showFolderMenu,
                    onDismissRequest = { showFolderMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("未選択（なし）") },
                        onClick = {
                            vm.setEditingFolder(null)
                            showFolderMenu = false
                        }
                    )
                    folders.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.name) },
                            onClick = {
                                vm.setEditingFolder(f.id)
                                showFolderMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        vm.saveEditing()
                        onClose()
                    },
                    enabled = editing != null
                ) { Text("保存") }

                OutlinedButton(
                    onClick = {
                        vm.deleteEditing()
                        onClose()
                    },
                    enabled = (editing?.id ?: 0L) != 0L
                ) { Text("削除") }
            }
        }
    }
}
