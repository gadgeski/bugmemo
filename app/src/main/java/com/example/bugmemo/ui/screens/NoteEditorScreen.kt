// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class) // ★ Added: Material3の一部APIにOpt-in

package com.example.bugmemo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack     // ★ Added: AutoMirroredな戻るアイコン
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle           // ★ Added: collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.NotesViewModel

/**
 * 単一ノートの編集画面（最小版）
 * - TopAppBarの戻るボタンで onBack() を呼ぶ
 * - タイトル/本文の編集、保存
 */
@Composable
fun NoteEditorScreen(
    vm: NotesViewModel = viewModel(),            // 既存VMを利用（FactoryはNav側/親側で指定していればOK）
    onBack: () -> Unit = {}                      // ★ Added: Navから受け取る戻るコールバック
) {
    val note by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(note?.title?.ifBlank { "(無題)" } ?: "新規ノート")
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {          // ★ Added: 戻る押下で呼び出し
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.saveEditing() }             // ひとまず“保存”のみ配置
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { inner ->
        EditorContent(                                     // 本文
            inner = inner,
            vm = vm
        )
    }
}

@Composable
private fun EditorContent(
    inner: PaddingValues,
    vm: NotesViewModel
) {
    val note by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier
            .padding(inner)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = note?.title.orEmpty(),
            onValueChange = { vm.setEditingTitle(it) },
            label = { Text("タイトル") },
            singleLine = true,
            modifier = Modifier.fillMaxSize().weight(0f, fill = false)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = note?.content.orEmpty(),
            onValueChange = { vm.setEditingContent(it) },
            label = { Text("内容") },
            minLines = 8,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, fill = true)
        )

        Spacer(Modifier.height(12.dp))

        // ここにフォルダ選択やスター切替、削除ボタン等を順次追加予定
        // 例：
        // Row { Button(onClick = { vm.deleteEditing(); onBack() }) { Text("削除") } }
    }
}
