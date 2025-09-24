// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/components/Common.kt
// ----------------------------------------
package com.example.bugmemo.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Note

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NoteRow(note: Note, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = note.title.ifBlank { "(無題)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            AssistChip(onClick = {}, label = { Text(note.folder ?: "Inbox") })
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = note.content.take(120),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPicker(
    folders: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = !open }) {
        OutlinedTextField(
            readOnly = true,
            value = selected?.ifBlank { "Inbox" } ?: "Inbox",
            onValueChange = {},
            label = { Text("フォルダ") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Inbox") }, onClick = { onSelected(null); open = false })
            folders.forEach { f ->
                DropdownMenuItem(text = { Text(f) }, onClick = { onSelected(f); open = false })
            }
        }
    }
}
