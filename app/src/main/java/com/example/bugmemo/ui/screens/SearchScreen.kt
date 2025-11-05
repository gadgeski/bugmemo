// app/src/main/java/com/example/bugmemo/ui/screens/SearchScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Changed: import は辞書順・未使用除去（ktlint/spotless を想定）
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel

// ★ Removed: 画面内での viewModel() 生成は廃止（親から渡されるため）
// import androidx.lifecycle.viewmodel.compose.viewModel
// ★ Added: Bugs へ戻るショートカット(androidx.compose.material.icons.automirrored.filled.List)
// ★ Added: Paging のロード状態(androidx.paging.LoadState)
// ★ Added: ローディング表示(androidx.compose.material3.CircularProgressIndicator)
// import androidx.compose.foundation.lazy.items // ★ Removed: Paging 置換のため未使用

/**
 * 検索画面（Bugs のクエリと同じ状態を共有）
 * - クエリ入力で VM の setQuery を更新
 * - 表示は Paging 版：vm.pagedNotes を collectAsLazyPagingItems で収集
 * - 結果タップで編集画面へ遷移（onOpenEditor）
 */
@Composable
fun SearchScreen(
    // ★ Changed: viewModel() のデフォルト生成をやめ、親（Nav）から渡す
    vm: NotesViewModel,
    // ★ keep: Editor に遷移するラムダ（Nav から受け取る）
    onOpenEditor: () -> Unit = {},
    // ★ Added: Bugs（Notes）へトップレベル遷移するラムダ（Nav から受け取る）
    onOpenNotes: () -> Unit = {},
) {
    val query by vm.query.collectAsStateWithLifecycle(initialValue = "")
    // ★ Added: Paging データを収集
    val results: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                // TODO: 必要なら strings.xml にリソース化可能
                actions = {
                    // ★ Added: Bugs へ戻るショートカット（Nav 側の共通ヘルパでトップレベル遷移）
                    IconButton(onClick = onOpenNotes) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Bugs",
                            // TODO: リソース化可能
                        )
                    }
                    // 検索フィールド（シンプル版）
                    OutlinedTextField(
                        value = query,
                        onValueChange = { vm.setQuery(it) },
                        singleLine = true,
                        placeholder = { Text("キーワードを入力") },
                        // TODO: リソース化可能
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                            // TODO: リソース化可能
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { vm.setQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                    // TODO: リソース化可能
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { /* VM 側の Flow により自動反映 */ },
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(280.dp),
                    )
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize(),
        ) {
            // ★ Changed: 未入力時は Paging リストを描画せずヒント表示
            if (query.isBlank()) {
                EmptyHint(
                    title = "検索ワードを入力してください",
                    // TODO: リソース化可能
                    subtitle = "例: クラッシュ / Retrofit / Compose",
                    // TODO: リソース化可能
                )
                return@Column
            }

            // ★ Added: Paging のロード状態に応じた分岐
            when (val state = results.loadState.refresh) {
                is LoadState.Loading -> {
                    // 初回ロード中
                    InitialLoading()
                }
                is LoadState.Error -> {
                    // 失敗時（簡易表示）
                    EmptyHint(
                        title = "読み込みに失敗しました",
                        subtitle = state.error.message ?: "不明なエラー",
                    )
                }
                is LoadState.NotLoading -> {
                    if (results.itemCount == 0) {
                        // 空結果
                        EmptyHint(
                            title = "0件でした", // TODO: リソース化可能
                            subtitle = "キーワードを変えて試してみましょう",
                            // TODO: リソース化可能
                        )
                    } else {
                        // 通常表示
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // ★ Changed: items(...) → Paging 版（index で取り出す）
                            items(
                                count = results.itemCount,
                                key = { index ->
                                    val item = results[index]
                                    item?.id ?: "placeholder-$index"
                                    // ★ Added: null（プレースホルダー）対策
                                },
                            ) { index ->
                                val note = results[index]
                                if (note == null) {
                                    // ★ Added: プレースホルダー（プレフェッチ中）
                                    ShimmerlessPlaceholderRow()
                                } else {
                                    ResultRow(
                                        note = note,
                                        onClick = {
                                            vm.loadNote(note.id)
                                            onOpenEditor()
                                        },
                                        onToggleStar = { vm.toggleStar(note.id, note.isStarred) },
                                    )
                                }
                            }

                            // ★ Added: 追加ロード中のフッター
                            if (results.loadState.append is LoadState.Loading) {
                                item(key = "append-loading") {
                                    AppendLoading()
                                }
                            }
                            if (results.loadState.append is LoadState.Error) {
                                item(key = "append-error") {
                                    AppendError()
                                }
                            }
                        }
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
    onToggleStar: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "(無題)" },
                    // TODO: リソース化可能（label_untitled 等）
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
                    // TODO: リソース化可能
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Not starred")
                    // TODO: リソース化可能
                }
            }
        }
    }
}

// ★ Added: 初回ロード表示（中央にインジケータ）
@Composable
private fun InitialLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("読み込み中…")
    }
}

// ★ Added: 追加ロードのフッター表示
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
        Spacer(Modifier.width(8.dp))
        Text("さらに読み込み中…")
    }
}

// ★ Added: 追加ロード失敗の簡易表示（必要なら再試行ボタンを追加）
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

// ★ Added: プレースホルダー行（簡易）
@Composable
private fun ShimmerlessPlaceholderRow() {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) { /* 簡易なので中身は空。必要なら灰色のボックス等を配置 */ }
}

@Composable
private fun EmptyHint(
    title: String,
    subtitle: String,
) {
    Surface(
        modifier = Modifier
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
