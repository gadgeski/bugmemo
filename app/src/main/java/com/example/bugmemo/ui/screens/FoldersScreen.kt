// app/src/main/java/com/example/bugmemo/ui/screens/FoldersScreen.kt
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.clickable              // ★ Added
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.ui.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    vm: NotesViewModel,
    onClose: () -> Unit = {}
) {
    val folders by vm.folders.collectAsStateWithLifecycle(emptyList())
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // 追加フォーム
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("新規フォルダ名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val name = newName.trim()
                        if (name.isNotEmpty()) {
                            vm.addFolder(name)
                            newName = ""
                        }
                    }
                ) { Text("追加") }
            }

            HorizontalDivider()

            if (folders.isEmpty()) {
                Text("フォルダがありません。上で追加してください。", modifier = Modifier.padding(8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(folders, key = { it.id }) { f ->
                        // ★ Changed: 行全体をクリックで「絞り込み→戻る」
                        ListItem(
                            headlineContent = {
                                Text(f.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.setFolderFilter(f.id)  // ★ ここで絞り込みIDをセット
                                    onClose()                  // ★ Bugs へ戻る
                                },
                            trailingContent = {
                                IconButton(onClick = { vm.deleteFolder(f.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete folder")
                                }
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
