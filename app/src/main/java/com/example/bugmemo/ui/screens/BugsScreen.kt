// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/screens/BugsScreen.kt
// ----------------------------------------
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.components.EmptyState
import com.example.bugmemo.ui.components.NoteRow

@Composable
fun BugsScreen(
    notes: List<Note>,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            EmptyState("まだバグはありません。右下の＋で作成")
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(notes) { note -> NoteRow(note) { onOpen(note.id) } }
            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            onClick = onCreate
        ) { Text("＋") }
    }
}