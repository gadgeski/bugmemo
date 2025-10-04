// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ▼▼ アイコン解決の“最小修正” ▼▼
// ▲▲ ここまで最小修正 ▲▲

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

/**
 * NoteEditorScreen（最小修正版）
 * - Icons の未解決を解消（Icons を import し、Icons.Xxx 参照に統一）
 * - TextField：タイトルは fillMaxWidth、本文は weight(1f) で縦に拡張（0 は使わない）
 */
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
                        // ★ Changed: 正しい参照パスで呼ぶ（AutoMirrored 推奨）
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            vm.saveEditing()
                            // 必要なら保存後に戻る
                            // onBack()
                        }
                    ) {
                        // ★ Changed: Icons.Filled.Save を利用（Icons を import 済み）
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

            // 本文：縦方向に広がる（0 を渡さない → weight(1f)）
            OutlinedTextField(
                value = editing?.content.orEmpty(),
                onValueChange = { vm.setEditingContent(it) },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)                // ★ Changed: > 0 を保証（0 は不可）
                    .heightIn(min = 160.dp),   // 読みやすさのため最低高（任意）
                minLines = 8
            )
        }
    }
}
