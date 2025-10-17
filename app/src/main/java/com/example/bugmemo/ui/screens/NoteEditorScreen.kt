// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Added: IME回避 で imePadding を使うための import を追加
// ★ keep: import ブロック内に行末コメントは入れない（ktlint 配慮）
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.ui.NotesViewModel

// ★ Added: stringResource を使うため追加(androidx.compose.ui.res.stringResource)
// ★ Added: R参照（strings.xml のキー参照に必要）
// ★ Removed: LaunchedEffect の import（自動 newNote 初期化は廃止）
// ★ Removed: 画面内での viewModel() 生成は廃止（呼び出し側から受け取るため）

@Composable
fun NoteEditorScreen(
    // ★ keep: 呼び出し側から同一 VM を受け取る（画面内での viewModel() 生成はしない）
    vm: NotesViewModel,
    onBack: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    // ★ keep: 編集対象が準備できるまで入力や保存を無効化
    val enabled = editing != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ★ Changed: タイトルのリソース化（空なら label_untitled / 新規なら title_new_note）
                    val titleText = editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                        ?: stringResource(R.string.title_new_note)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            // ★ Changed: CD をリソース化
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.saveEditing() },
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            // ★ Changed: CD をリソース化
                            contentDescription = stringResource(R.string.cd_save),
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
                .verticalScroll(rememberScrollState())
                .imePadding(),
            // ★ Added: ソフトキーボード表示時の下部被りを自動回避
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // タイトル
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                onValueChange = { text -> vm.setEditingTitle(text) },
                // ★ Changed: ラベルのリソース化
                label = { Text(stringResource(R.string.label_title)) },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )

            // 本文
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { text -> vm.setEditingContent(text) },
                // ★ Changed: ラベルのリソース化
                label = { Text(stringResource(R.string.label_content)) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // ★ keep: エディタ領域をできるだけ広く
                    .heightIn(min = 160.dp),
                // ★ keep: 読みやすさのための最小高さ
                minLines = 8,
            )
        }
    }
}
