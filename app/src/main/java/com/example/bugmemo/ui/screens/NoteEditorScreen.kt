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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.ui.NotesViewModel

// ★ Removed: LaunchedEffect の import（自動 newNote 初期化を廃止）
// import androidx.compose.runtime.LaunchedEffect
// ★ Removed: 画面内での viewModel() 生成は廃止（呼び出し側から受け取るため）
// import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NoteEditorScreen(
    // ★ Changed: デフォルトの viewModel() を削除。呼び出し側（Nav/AppScaffold）から同一 VM を受け取る。
    vm: NotesViewModel,
    onBack: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    // ★ Removed: 入場時の自動 newNote() 初期化。
    // （検索→編集遷移直後に上書きされる事故を防ぐため。新規は呼び出し側で vm.newNote() 後に遷移する運用へ統一）

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
            // ★ Changed: onValueChange 内の newNote() 呼び出しは削除（不要かつ副作用の原因）
            // タイトル
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                onValueChange = { text -> vm.setEditingTitle(text) },
                label = { Text("タイトル") },
                singleLine = true,
                // ★ keep: 準備完了まで入力不可
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )

            // ★ Changed: onValueChange 内の newNote() 呼び出しは削除（不要かつ副作用の原因）
            // 本文
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { text -> vm.setEditingContent(text) },
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
