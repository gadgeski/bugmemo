// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ▼ 不要だったプレースホルダコメントを削除（lint対策） // ★ Removed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun NoteEditorScreen(
    vm: NotesViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = editing?.title?.ifBlank { "(無題)" } ?: "新規メモ",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.saveEditing() },
                        enabled = editing != null // ★ Added: 編集対象が無いときは保存を無効化（任意）
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // タイトル
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                onValueChange = { vm.setEditingTitle(it) },
                label = { Text("タイトル") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 本文
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { vm.setEditingContent(it) },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)              // ★ keep: 縦に広がる
                    .heightIn(min = 160.dp), // 読みやすさのための最小高さ
                minLines = 8
            )
        }
    }
}
