// app/src/main/java/com/example/bugmemo/ui/screens/AllNotesScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ keep: import ブロック内に行末コメントは入れない（ktlint 配慮）
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

// ★ keep: Paging Compose 依存(LazyPagingItems/collectAsLazyPagingItems)
// ★ keep: ローディング表示(androidx.compose.material3.CircularProgressIndicator)

@Composable
fun AllNotesScreen(
    // ★ keep: Nav から渡す
    onBack: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    // vm: NotesViewModel = viewModel(), // ★ Removed: 画面内生成は避け、親から受け取る
    vm: NotesViewModel, // ★ keep: 親から受け取る（重複 VM 防止）
) {
    // ★ keep: Flow<PagingData<Note>> を収集
    val notesPaging: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

    // ★ keep: “スターのみ”の簡易フィルタ（画面ローカル）
    val starredOnly = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // ★ keep: スターのみ表示トグル
                    FilterChip(
                        // ★ 2. 読み取りは .value
                        selected = starredOnly.value,
                        onClick = { starredOnly.value = !starredOnly.value },
                        // ★ 書き込みも .value
                        label = { Text(if (starredOnly.value) "Starred only" else "All") },
                    )
                },
            )
        },
    ) { inner ->
        // ★ keep: 初回ロード／エラー／空表示の分岐
        when (val state = notesPaging.loadState.refresh) {
            is LoadState.Loading -> {
                InitialLoading(modifier = Modifier.padding(inner))
            }

            is LoadState.Error -> {
                EmptyAllNotesHint(
                    modifier = Modifier.padding(inner),
                    title = "読み込みに失敗しました",
                    subtitle = state.error.message ?: "不明なエラー",
                )
            }

            is LoadState.NotLoading -> {
                if (notesPaging.itemCount == 0) {
                    // ★ keep: 空状態（Paging の件数で判定）
                    EmptyAllNotesHint(Modifier.padding(inner))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(inner)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // ★ keep: items(list) → items(count) に置換済み
                        items(
                            count = notesPaging.itemCount,
                            key = { index ->
                                val item = notesPaging[index]
                                item?.id ?: "placeholder-$index" // ★ keep: null プレースホルダ対応
                            },
                        ) { index ->
                            val note = notesPaging[index]
                            if (note == null) {
                                PlaceholderRow()
                            } else {
                                // ★ keep: 画面ローカルのスター絞り込み
                                if (!starredOnly.value || note.isStarred) {
                                    AllNoteRow(
                                        note = note,
                                        onClick = {
                                            // ★ Changed: AllNote → Editor ルートで必ず vm.loadNote(id) を先に呼ぶ
                                            vm.loadNote(note.id)
                                            onOpenEditor()
                                        },
                                        onToggleStar = {
                                            vm.toggleStar(
                                                note.id,
                                                note.isStarred,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                        // ★ keep: 追加ロードのフッター表示
                        if (notesPaging.loadState.append is LoadState.Loading) {
                            item("append-loading") { AppendLoading() }
                        }
                        if (notesPaging.loadState.append is LoadState.Error) {
                            item("append-error") { AppendError() }
                        }
                    }
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

/* ▼▼ ここから Empty/Loading/Placeholder ▼▼ */

@Composable
private fun EmptyAllNotesHint(
    modifier: Modifier = Modifier,
    title: String = "メモがありません",
    subtitle: String = "作成したメモがここに一覧表示されます",
) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InitialLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("読み込み中…")
    }
}

@Composable
private fun AppendLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(8.dp))
        Text("さらに読み込み中…")
    }
}

@Composable
private fun AppendError() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("読み込みに失敗しました")
    }
}

@Composable
private fun PlaceholderRow() {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        // ★ keep: 簡易プレースホルダー。必要ならスケルトンを追加
    }
}

/* ▲▲ ここまで Empty/Loading/Placeholder ▲▲ */
