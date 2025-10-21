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
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.core.AppLocaleManager
import com.example.bugmemo.ui.NotesViewModel

// ★ Added: 太字トグル用アイコン(androidx.compose.material.icons.filled.FormatBold)
// ★ Added: TextFieldValue のローカル状態(androidx.compose.runtime.mutableStateOf)
// ★ Added: DataStore購読に必要(androidx.compose.ui.platform.LocalContext)
// ★ Added: キャレット/選択制御(androidx.compose.ui.text.TextRange)
// ★ Added: 選択範囲を扱うため(androidx.compose.ui.text.input.TextFieldValue)
// ★ Added: editor_font_scale 購読(com.example.bugmemo.core.AppLocaleManager)
// ★ Added: stringResource を使うため追加(androidx.compose.ui.res.stringResource)
// ★ Added: R参照（strings.xml のキー参照に必要）
// ★ Removed: LaunchedEffect の import（自動 newNote 初期化は廃止）
// ★ Removed: 画面内での viewModel() 生成は廃止（呼び出し側から受け取るため）

@Composable
fun NoteEditorScreen(
    // ★ keep: 呼び出し側から同一 VM を受け取る（画面内での viewModel() 生成はしない）
    vm: NotesViewModel,
    onBack: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)

    // ★ keep: 編集対象が準備できるまで入力や保存を無効化
    val enabled = editing != null

    // ----------------------------------------
    // ★ Added: フォントスケール（DataStore）を購読
    // ----------------------------------------
    val context = LocalContext.current
    // ★ Added
    val fontScale by AppLocaleManager.editorFontScaleFlow(context)
        .collectAsStateWithLifecycle(initialValue = 1.0f)
    // ★ Added

    // ----------------------------------------
    // ★ Added: 本文フィールドは選択範囲操作が必要なため TextFieldValue をローカルで保持
    //          editing が差し替わった場合のみ初期化し、タイプ中は上書きしない
    // ----------------------------------------
    var contentField by remember(editing?.id) {
        mutableStateOf(
            TextFieldValue(
                text = editing?.content.orEmpty(),
                selection = TextRange(editing?.content?.length ?: 0),
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ★ Changed: タイトルのリソース化（空なら label_untitled / 新規なら title_new_note）
                    val titleText = editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                        ?: stringResource(R.string.title_new_note)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            // ★ keep: CD はリソース化済み
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    // ----------------------------------------
                    // ★ Added: Markdown 太字(B) トグルボタン（最小）
                    //          選択範囲を **…** で囲む／すでに **…** を選択している場合は外す
                    //          未選択時は「****」を挿入し、キャレットを中央へ移動
                    // ----------------------------------------
                    IconButton(
                        onClick = {
                            val value = contentField
                            val text = value.text
                            val sel = value.selection

                            // 何も編集中でなければ無視
                            if (!enabled) return@IconButton

                            val newValue: TextFieldValue = if (!sel.collapsed) {
                                // 選択あり：包む or 外す
                                val selected = text.substring(sel.start, sel.end)
                                if (selected.startsWith("**") && selected.endsWith("**") && selected.length >= 4) {
                                    // ★ Added: すでに **…** を含む範囲を選択 → アンラップ
                                    val inner = selected.removePrefix("**").removeSuffix("**")
                                    val newText = text.substring(0, sel.start) + inner + text.substring(sel.end)
                                    val newCursor = sel.start + inner.length
                                    TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newCursor, newCursor),
                                    )
                                } else {
                                    // ★ Added: **…** で囲む
                                    val newText = text.substring(0, sel.start) + "**" + selected + "**" + text.substring(sel.end)
                                    val newCursor = sel.end + 4
                                    // 囲ったあとの末尾へ
                                    TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newCursor, newCursor),
                                    )
                                }
                            } else {
                                // 選択なし：**** を挿入してキャレットを中央へ
                                val insertPos = sel.start
                                val newText = text.substring(0, insertPos) + "****" + text.substring(insertPos)
                                val caret = insertPos + 2 // "**|**" の中央へ
                                TextFieldValue(
                                    text = newText,
                                    selection = TextRange(caret, caret),
                                )
                            }

                            contentField = newValue
                            vm.setEditingContent(newValue.text)
                                  // ★ Added: VM へ反映
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FormatBold,
                            contentDescription = null,
                            // ★ Changed: 文字列リソース未追加のため一旦 null（後で strings 追加可）
                        )
                    }

                    // ★ keep: 保存
                    IconButton(
                        onClick = { vm.saveEditing() },
                        enabled = enabled,
                    ) {
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
                // ★ keep: ラベルのリソース化
                label = { Text(stringResource(R.string.label_title)) },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                // ★ Added: フォントスケールを適用（タイトルは少し大きめの typography をベースに）
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale,
                ),
            )

            // 本文（選択範囲対応）
            OutlinedTextField(
                value = contentField,
                // ★ Changed: String → TextFieldValue
                onValueChange = { v ->
                    contentField = v
                    if (enabled) vm.setEditingContent(v.text)
                                // ★ keep: VM へ反映
                },
                // ★ keep: ラベルのリソース化
                label = { Text(stringResource(R.string.label_content)) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // ★ keep: エディタ領域をできるだけ広く
                    .heightIn(min = 160.dp),
                // ★ keep: 読みやすさのための最小高さ
                minLines = 8,
                // ★ Added: フォントスケール適用（本文は bodyLarge ベース）
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                ),
            )
        }
    }
}
