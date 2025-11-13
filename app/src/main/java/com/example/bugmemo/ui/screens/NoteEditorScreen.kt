// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ keep: import ブロック内に行末コメントは入れない（ktlint 配慮）
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.core.AppLocaleManager
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.common.MarkdownBoldVisualTransformation
// ★ 色(androidx.compose.material.icons.filled.FormatColorText)
// ★ 下線(androidx.compose.material.icons.filled.FormatUnderlined)
// ★ 見出し(androidx.compose.material.icons.filled.Title)
// ★ メニュー(DropdownMenu/DropdownMenuItem)
// ★ VT:中身をリッチに(com.example.bugmemo.ui.common.MarkdownBoldVisualTransformation)
// ★ TextOverflow:タイトルを省略表示にする
// ★ List: 一覧画面(AllNotes)へのナビゲーションアイコン
// ★ Added: 一覧アイコンの AutoMirrored 版(androidx.compose.material.icons.automirrored.filled.List)

@Composable
fun NoteEditorScreen(
    // ★ keep: 呼び出し側から同一 VM を受け取る（画面内での viewModel() 生成はしない）
    vm: NotesViewModel,
    onBack: () -> Unit = {},
    onOpenAllNotes: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    // ★ keep: 編集対象が準備できるまで入力や保存を無効化
    val enabled = editing != null

    // ----------------------------------------
    // ★ keep: フォントスケール（DataStore）を購読
    // ----------------------------------------
    val context = LocalContext.current
    val fontScale by AppLocaleManager.editorFontScaleFlow(context)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    // ----------------------------------------
    // ★ keep: 本文は TextFieldValue（選択範囲のため）
    // ----------------------------------------
    var contentField by remember(editing?.id) {
        mutableStateOf(
            TextFieldValue(
                text = editing?.content.orEmpty(),
                selection = TextRange(editing?.content?.length ?: 0),
            ),
        )
    }

    // ───── 追加: 編集ユーティリティ（包む/外す） ─────
    // ★ keep: [u]..[/u] や [color=#xxxxxx]..[/color] 等で共通利用
    fun wrapOrUnwrap(value: TextFieldValue, open: String, close: String): TextFieldValue {
        val t = value.text
        val sel = value.selection
        return if (!sel.collapsed) {
            val selected = t.substring(sel.start, sel.end)
            val isWrapped = selected.startsWith(open) && selected.endsWith(close)
            if (isWrapped) {
                val inner = selected.removePrefix(open).removeSuffix(close)
                val newText = t.replaceRange(sel.start, sel.end, inner)
                val pos = sel.start + inner.length
                value.copy(text = newText, selection = TextRange(pos, pos))
            } else {
                val wrapped = open + selected + close
                val newText = t.replaceRange(sel.start, sel.end, wrapped)
                val pos = sel.start + wrapped.length
                value.copy(text = newText, selection = TextRange(pos, pos))
            }
        } else {
            val pos = sel.start
            val inserted = open + close
            // ★ Changed: 未選択時の挿入位置を修正（0..pos / pos..end）
            val newText = t.substring(pos) + inserted + t.substring(pos)
            val caret = pos + open.length
            value.copy(text = newText, selection = TextRange(caret, caret))
        }
    }

    // ★ keep: 行頭見出し (# / ## / ###) のトグル
    fun toggleHeading(level: Int) {
        val t = contentField.text
        val sel = contentField.selection
        val lineStart = t.lastIndexOf('\n', startIndex = (sel.start - 1).coerceAtLeast(0)) + 1
        val lineEnd = t.indexOf('\n', startIndex = sel.end).let { if (it == -1) t.length else it }
        val line = t.substring(lineStart, lineEnd)
        val stripped = line.replace(Regex("^#{1,6}\\s+"), "")
        val prefix = "#".repeat(level) + " "
        val newLine = if (line.startsWith(prefix)) stripped else prefix + stripped
        val newText = t.replaceRange(lineStart, lineEnd, newLine)
        val delta = newLine.length - line.length
        val newSel = TextRange(
            (sel.start + delta).coerceAtLeast(0),
            (sel.end + delta).coerceAtLeast(0),
        )
        val nv = contentField.copy(text = newText, selection = newSel)
        contentField = nv
        vm.setEditingContent(contentField.text)
        // ★ keep: 直後の値を参照して警告回避
    }

    // ────────────────────────────────────────
    // ★ keep: 色/見出しメニューの state（MutableState を直接保持）
    val colorMenu = remember { mutableStateOf(false) }
    val headingMenu = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ★ keep: タイトルのリソース化
                    val titleText = editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                        ?: stringResource(R.string.title_new_note)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    // ----------------------------------------
                    // ★ keep: 太字(B)
                    // ----------------------------------------
                    IconButton(
                        enabled = enabled,
                        onClick = {
                            val text = contentField.text
                            val sel = contentField.selection
                            if (!enabled) return@IconButton
                            val newValue: TextFieldValue =
                                if (!sel.collapsed) {
                                    val selected = text.substring(sel.start, sel.end)
                                    if (selected.startsWith("**") && selected.endsWith("**") && selected.length >= 4) {
                                        val inner = selected.removePrefix("**").removeSuffix("**")
                                        val newText = text.take(sel.start) + inner + text.drop(sel.end)
                                        val newCursor = sel.start + inner.length
                                        TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newCursor, newCursor),
                                        )
                                    } else {
                                        val newText =
                                            text.take(sel.start) + "**" + selected + "**" + text.drop(sel.end)
                                        val newCursor = sel.end + 4
                                        TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newCursor, newCursor),
                                        )
                                    }
                                } else {
                                    val insertPos = sel.start
                                    val newText = text.take(insertPos) + "****" + text.drop(insertPos)
                                    val caret = insertPos + 2
                                    TextFieldValue(text = newText, selection = TextRange(caret, caret))
                                }
                            contentField = newValue
                            vm.setEditingContent(contentField.text)
                            // ★ keep: 直後の値を反映
                        },
                    ) {
                        Icon(imageVector = Icons.Filled.FormatBold, contentDescription = null)
                    }

                    // ----------------------------------------
                    // ★ keep: 下線 [u]...[/u]
                    // ----------------------------------------
                    IconButton(
                        enabled = enabled,
                        onClick = {
                            val nv = wrapOrUnwrap(contentField, "[u]", "[/u]")
                            contentField = nv
                            vm.setEditingContent(contentField.text)
                        },
                    ) {
                        Icon(imageVector = Icons.Filled.FormatUnderlined, contentDescription = null)
                    }

                    // ----------------------------------------
                    // ★ keep: 色 [color=#RRGGBB]...[/color]
                    // ----------------------------------------
                    IconButton(
                        enabled = enabled,
                        onClick = { colorMenu.value = true },
                    ) {
                        Icon(imageVector = Icons.Filled.FormatColorText, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = colorMenu.value,
                        onDismissRequest = { colorMenu.value = false },
                    ) {
                        // ★ keep: 簡易パレット
                        listOf("#ff5252", "#ff9800", "#4caf50", "#2196f3").forEach { hex ->
                            DropdownMenuItem(
                                text = { Text(hex) },
                                onClick = {
                                    val nv = wrapOrUnwrap(contentField, "[color=$hex]", "[/color]")
                                    contentField = nv
                                    vm.setEditingContent(contentField.text)
                                    colorMenu.value = false
                                },
                            )
                        }
                    }

                    // ----------------------------------------
                    // ★ keep: 見出し (# / ## / ###)
                    // ----------------------------------------
                    IconButton(
                        enabled = enabled,
                        onClick = { headingMenu.value = true },
                    ) {
                        Icon(imageVector = Icons.Filled.Title, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = headingMenu.value,
                        onDismissRequest = { headingMenu.value = false },
                    ) {
                        (1..3).forEach { level ->
                            DropdownMenuItem(
                                text = { Text("H$level") },
                                onClick = {
                                    toggleHeading(level)
                                    headingMenu.value = false
                                },
                            )
                        }
                    }

                    // ----------------------------------------
                    // ★ Changed: 一覧へ戻るアイコンを AutoMirrored 版に差し替え
                    // ----------------------------------------
                    IconButton(onClick = onOpenAllNotes) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            // ★ TODO: 必要なら strings.xml に CD を追加
                        )
                    }

                    // ★ keep: 保存
                    IconButton(onClick = { vm.saveEditing() }, enabled = enabled) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.cd_save),
                        )
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            // ★ keep: ソフトキーボード表示時の下部被りを自動回避
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // タイトル
            OutlinedTextField(
                value = editing?.title.orEmpty(),
                onValueChange = { text -> vm.setEditingTitle(text) },
                label = { Text(stringResource(R.string.label_title)) },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale,
                ),
            )
            // 本文（選択範囲対応）
            OutlinedTextField(
                value = contentField,
                onValueChange = { v ->
                    contentField = v
                    if (enabled) vm.setEditingContent(v.text)
                },
                label = { Text(stringResource(R.string.label_content)) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 160.dp),
                minLines = 8,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                ),
                visualTransformation = MarkdownBoldVisualTransformation(
                    hideMarkers = true,
                    // ★ keep: 記号を隠して装飾のみ見せる
                ),
            )
        }
    }
}
