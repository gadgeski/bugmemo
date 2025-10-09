// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun NoteEditorScreen(
    vm: NotesViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    // ★ Changed: 初回だけ newNote() を呼ぶ（編集中データが無い場合の自動初期化）
    LaunchedEffect(Unit) {
        if (editing == null) vm.newNote()
    }

    // ★ keep: 編集対象が準備できるまで入力や保存を無効化
    val enabled = editing != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = editing?.title?.ifBlank { "(無題)" } ?: "新規メモ",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.saveEditing() },
                        // ★ keep: 編集対象が無いときは保存を無効化
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save",
                        )
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // タイトル
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                // ★ Added: ここで typing 開始時にも保険として初期化をかける
                onValueChange = { text ->
                    // ★ Added: まだ null なら即時に newNote() を呼ぶ（LaunchedEffect の取りこぼし対策）
                    if (editing == null) vm.newNote()
                    vm.setEditingTitle(text)
                },
                label = { Text("タイトル") },
                singleLine = true,
                // ★ keep: 準備完了まで入力不可
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )

            // 本文
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                // ★ Added: こちらも同様に保険で初期化
                onValueChange = { text ->
                    // ★ Added: まだ null なら即時に newNote() を呼ぶ
                    if (editing == null) vm.newNote()
                    vm.setEditingContent(text)
                },
                label = { Text("内容") },
                // ★ keep: 準備完了まで入力不可
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    // ★ keep: 縦に広がる
                    .weight(1f)
                    // 読みやすさのための最小高さ
                    .heightIn(min = 160.dp),
                minLines = 8,
            )
        }
    }
}
