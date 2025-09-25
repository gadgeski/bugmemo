// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/screens/FoldersScreen.kt
// ----------------------------------------
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FoldersScreen(
    folders: List<String>,
    onAddFolder: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("新規フォルダ名") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (name.isNotBlank()) { onAddFolder(name.trim()); name = "" } }) { Text("追加") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(folders) { f ->
                Text(text = f, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
                Divider()
            }
        }
    }
}
