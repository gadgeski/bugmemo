@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Added: 一覧専用の軽量画面（小PR向け）
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

@Composable
fun AllNotesScreen(
    // ★ keep: Nav から渡す
    onBack: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    vm: NotesViewModel = viewModel(),
) {
    val notes by vm.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    // ★ Added: “スターのみ”の簡易フィルタ（画面ローカル・低リスク）
    var starredOnly by remember { mutableStateOf(false) }
    val list = if (starredOnly) notes.filter { it.isStarred } else notes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Notes") },
                // ★ 小PRのため文字列は直書き（必要なら strings に昇格）
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ★ Added: スターのみ表示トグル（軽量な視認性改善）
                    FilterChip(
                        selected = starredOnly,
                        onClick = { starredOnly = !starredOnly },
                        label = { Text(if (starredOnly) "Starred only" else "All") },
                    )
                },
            )
        },
    ) { inner ->
        if (list.isEmpty()) {
            EmptyAllNotesHint(Modifier.padding(inner))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list, key = { it.id }) { note ->
                    AllNoteRow(
                        note = note,
                        onClick = {
                            vm.loadNote(note.id)
                            onOpenEditor()
                        },
                        onToggleStar = { vm.toggleStar(note.id, note.isStarred) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AllNoteRow(
    note: Note,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "(無題)" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(Icons.Filled.Star, contentDescription = "Starred")
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Not starred")
                }
            }
        }
    }
}

@Composable
private fun EmptyAllNotesHint(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        tonalElevation = 0.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("メモがありません", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "作成したメモがここに一覧表示されます",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
