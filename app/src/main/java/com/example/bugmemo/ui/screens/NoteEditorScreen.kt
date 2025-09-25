// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
// ----------------------------------------
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.components.FolderPicker

@Composable
fun NoteEditorScreen(
    note: Note,
    folders: List<String>,
    onSave: (String, String, String?) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var folder by remember { mutableStateOf(note.folder) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("タイトル（バグ要約）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("詳細（再現 / 期待 / 実際 / ログ など）") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            minLines = 8
        )
        Spacer(Modifier.height(10.dp))
        FolderPicker(folders = folders, selected = folder, onSelected = { folder = it })
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = { onSave(title.trim(), content.trim(), folder) }) {
                Text("保存")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("削除") }
        }
    }
}
