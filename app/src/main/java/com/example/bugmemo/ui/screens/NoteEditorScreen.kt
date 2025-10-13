// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Added: スクロール対応のため foundation の API を使用（import ブロックにはコメントを挟まない）
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

// ★ Removed: LaunchedEffect の import（自動 newNote 初期化は廃止）
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

    // ★ Removed: 入場時の自動 newNote() 初期化（検索→編集直後の上書き事故を防止）

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            // ★ Added: 画面全体（エディタ領域）を縦スクロール可能に
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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

            // 本文
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { text -> vm.setEditingContent(text) },
                label = { Text("内容") },
                // ★ keep: 準備完了まで入力不可
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    // ★ keep: エディタ内である程度の高さを確保
                    .weight(1f)
                    // 読みやすさのための最小高さ
                    .heightIn(min = 160.dp),
                minLines = 8,
            )
        }
    }
}
