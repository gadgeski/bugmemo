// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/screens/SearchScreen.kt
// ----------------------------------------
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.components.EmptyState
import com.example.bugmemo.ui.components.NoteRow

@Composable
fun SearchScreen(
    onQuery: (String) -> List<Note>,
    results: List<Note>,
    onOpen: (Long) -> Unit
) {
    var q by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            label = { Text("メモを検索") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        val filtered = remember(q) { if (q.isBlank()) emptyList() else onQuery(q) }
        when {
            q.isBlank() -> EmptyState("キーワードを入力してください")
            filtered.isEmpty() -> EmptyState("該当なし")
            else -> LazyColumn(Modifier.fillMaxSize()) { items(filtered) { n -> NoteRow(n) { onOpen(n.id) } } }
        }
    }
}
