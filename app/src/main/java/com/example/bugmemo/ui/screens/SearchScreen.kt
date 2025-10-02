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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * SearchScreen（完成版）
 * - 0件プレースホルダ
 * - IME アクション = Search（Enterでフォーカス解除）
 * - クリアボタン（×）で query を空に
 * - スクロール開始でキーボードを閉じる
 * - a11y: contentDescription を付与
 */
@Composable
fun SearchScreen(
    vm: NotesViewModel = viewModel(),
    onOpenEditor: () -> Unit = {}                               // ★ Added: 検索結果からエディタへ
) {
    val query by vm.query.collectAsStateWithLifecycle("")        // ★ Added: 既存 VM の状態を利用
    val results by vm.notes.collectAsStateWithLifecycle(emptyList()) // ★ Added: フィルタ×検索後の一覧を再利用
    val focusManager = LocalFocusManager.current                 // ★ Added
    val listState = rememberLazyListState()                      // ★ Added

    // ★ Added: スクロール開始でキーボードを閉じる
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collectLatest { scrolling ->
            if (scrolling) focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ───────────────── 検索ボックス ─────────────────
            OutlinedTextField(
                value = query,
                onValueChange = { vm.setQuery(it) },             // ★ Added: 入力のたびに保存（DataStore連携）
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("キーワードを入力") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "検索"               // ★ a11y
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { vm.setQuery(""); focusManager.clearFocus() } // ★ クリア
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "検索ワードをクリア" // ★ a11y
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // ★ IME Search
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }      // ★ Enterで閉じる
                )
            )

            HorizontalDivider()

            // ───────────────── 検索結果 ─────────────────
            when {
                query.isBlank() && results.isEmpty() -> {
                    // ★ 0件プレースホルダ（未入力）
                    EmptyMessage(
                        title = "検索キーワードを入力してください",
                        subtitle = "例: 「クラッシュ」「500」「Compose」など"
                    )
                }
                query.isNotBlank() && results.isEmpty() -> {
                    // ★ 0件プレースホルダ（ヒットなし）
                    EmptyMessage(
                        title = "該当するメモはありません",
                        subtitle = "条件を変えるか、別のキーワードを試してください"
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results, key = { it.id }) { note ->
                            SearchResultRow(
                                note = note,
                                onClick = {
                                    vm.loadNote(note.id)
                                    onOpenEditor()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    note: Note,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
        }
    }
}

@Composable
private fun EmptyMessage(
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
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
