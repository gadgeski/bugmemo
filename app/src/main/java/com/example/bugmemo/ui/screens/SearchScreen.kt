// app/src/main/java/com/example/bugmemo/ui/screens/SearchScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

/**
 * 検索画面（Bugs のクエリと同じ状態を共有）
 * - クエリ入力で VM の setQuery を更新
 * - 表示は VM.notes（内部で observeNotes/searchNotes を切替）
 * - 結果タップで編集画面へ遷移（onOpenEditor）
 */
@Composable
fun SearchScreen(
    vm: NotesViewModel = viewModel(),
    onOpenEditor: () -> Unit = {}                   // ★ Added: Editor に遷移するコールバック（Nav から受け取る）
) {
    val query by vm.query.collectAsStateWithLifecycle(initialValue = "")
    val results by vm.notes.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                actions = {
                    // 検索フィールド（シンプル版）
                    OutlinedTextField(
                        value = query,
                        onValueChange = { vm.setQuery(it) },
                        singleLine = true,
                        placeholder = { Text("キーワードを入力") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { vm.setQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),   // ★ Added
                        keyboardActions = KeyboardActions(                                  // ★ Added
                            onSearch = { /* 特に何もしなくても Flow が反映 */ }
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(280.dp)
                    )
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            if (query.isBlank()) {
                EmptyHint(
                    title = "検索ワードを入力してください",
                    subtitle = "例: クラッシュ / Retrofit / Compose"
                )
            } else if (results.isEmpty()) {
                EmptyHint(
                    title = "0件でした",
                    subtitle = "キーワードを変えて試してみましょう"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { it.id }) { note ->
                        ResultRow(
                            note = note,
                            onClick = {
                                vm.loadNote(note.id)     // 編集対象をロード
                                onOpenEditor()           // ★ Added: Editor へ遷移
                            },
                            onToggleStar = { vm.toggleStar(note.id, note.isStarred) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    note: Note,
    onClick: () -> Unit,
    onToggleStar: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "(無題)" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
private fun EmptyHint(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
