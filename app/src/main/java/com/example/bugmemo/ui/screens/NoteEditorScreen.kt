// app/src/main/java/com/example/bugmemo/ui/screens/NoteEditorScreen.kt

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.core.AppLocaleManager
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.common.MarkdownBoldVisualTransformation
import com.example.bugmemo.ui.theme.IceCyan
import com.example.bugmemo.ui.theme.IceDeepNavy
import com.example.bugmemo.ui.theme.IceGlassBorder
import com.example.bugmemo.ui.theme.IceGlassSurface
import com.example.bugmemo.ui.theme.IceHorizon
import com.example.bugmemo.ui.theme.IceSilver
import com.example.bugmemo.ui.theme.IceSlate
import com.example.bugmemo.ui.theme.IceTextPrimary
import com.example.bugmemo.ui.theme.IceTextSecondary

// ★ Changed: ワイルドカードインポートをやめ、個別インポートに変更(com.example.bugmemo.ui.theme.IceCyanなど)

@Composable
fun NoteEditorScreen(
    vm: NotesViewModel,
    onBack: () -> Unit = {},
    onOpenAllNotes: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    val enabled = editing != null

    val context = LocalContext.current
    val fontScale by AppLocaleManager.editorFontScaleFlow(context)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    var contentField by remember(editing?.id) {
        mutableStateOf(
            TextFieldValue(
                text = editing?.content.orEmpty(),
                selection = TextRange(editing?.content?.length ?: 0),
            ),
        )
    }

    // ───── 編集ユーティリティ（変更なし） ─────
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
            val newText = t.take(pos) + inserted + t.drop(pos)
            val caret = pos + open.length
            return value.copy(text = newText, selection = TextRange(caret, caret))
        }
    }

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
        contentField = contentField.copy(text = newText, selection = newSel)
        vm.setEditingContent(contentField.text)
    }

    val colorMenu = remember { mutableStateOf(false) }
    val headingMenu = remember { mutableStateOf(false) }

    // 深海グラデーション背景を作成
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                IceHorizon,
                IceSlate,
                IceDeepNavy,
            ),
        )
    }

    // Boxでラップして背景を適用
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            // Scaffold背景を透明に
            containerColor = Color.Transparent,
            // ステータスバー考慮
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                TopAppBar(
                    title = {
                        val titleText = editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                            ?: stringResource(R.string.title_new_note)
                        Text(
                            text = titleText,
                            // 等幅フォントで見出しっぽく
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    // AppBar自体も透明にし、アイコン色をIcebergテーマに合わせる
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = IceTextPrimary,
                        actionIconContentColor = IceSilver,
                        navigationIconContentColor = IceCyan,
                        // 戻るボタンはシアンで光らせる
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    },
                    actions = {
                        // Bold
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
                            },
                        ) {
                            Icon(imageVector = Icons.Filled.FormatBold, contentDescription = null)
                        }

                        // Underline
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

                        // Color
                        IconButton(
                            enabled = enabled,
                            onClick = { colorMenu.value = true },
                        ) {
                            Icon(imageVector = Icons.Filled.FormatColorText, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = colorMenu.value,
                            onDismissRequest = { colorMenu.value = false },
                            // メニュー背景を鉄色にし、枠線を少しつける
                            containerColor = IceSlate,
                            border = BorderStroke(1.dp, IceGlassBorder),
                        ) {
                            listOf("#ff5252", "#ff9800", "#4caf50", "#2196f3").forEach { hex ->
                                DropdownMenuItem(
                                    text = { Text(hex, color = IceTextPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                                    onClick = {
                                        val nv = wrapOrUnwrap(contentField, "[color=$hex]", "[/color]")
                                        contentField = nv
                                        vm.setEditingContent(contentField.text)
                                        colorMenu.value = false
                                    },
                                )
                            }
                        }

                        // Heading
                        IconButton(
                            enabled = enabled,
                            onClick = { headingMenu.value = true },
                        ) {
                            Icon(imageVector = Icons.Filled.Title, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = headingMenu.value,
                            onDismissRequest = { headingMenu.value = false },
                            containerColor = IceSlate,
                            border = BorderStroke(1.dp, IceGlassBorder),
                        ) {
                            (1..3).forEach { level ->
                                DropdownMenuItem(
                                    text = { Text("H$level", color = IceTextPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                                    onClick = {
                                        toggleHeading(level)
                                        headingMenu.value = false
                                    },
                                )
                            }
                        }

                        // List Icon
                        IconButton(onClick = onOpenAllNotes) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                            )
                        }

                        // Save (High Tech Accent)
                        IconButton(onClick = { vm.saveEditing() }, enabled = enabled) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = stringResource(R.string.cd_save),
                                // 有効時はCyanで発光させる
                                tint = if (enabled) IceCyan else IceSilver.copy(alpha = 0.5f),
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // タイトル入力
                TextField(
                    value = editing?.title.orEmpty(),
                    onValueChange = { text -> vm.setEditingTitle(text) },
                    placeholder = {
                        Text(
                            stringResource(R.string.label_title),
                            color = IceTextSecondary.copy(alpha = 0.5f),
                        )
                    },
                    singleLine = true,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    // タイトルはシアンで強調、等幅フォント
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize * fontScale,
                        color = IceCyan,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = IceCyan,
                        // カーソルもシアン
                    ),
                )

                // 本文入力 (Glass Console Style)
                TextField(
                    value = contentField,
                    onValueChange = { v ->
                        contentField = v
                        if (enabled) vm.setEditingContent(v.text)
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.label_content),
                            color = IceTextSecondary.copy(alpha = 0.5f),
                        )
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 300.dp)
                        // ガラスの枠線を追加
                        .border(
                            BorderStroke(1.dp, IceGlassBorder),
                            RoundedCornerShape(12.dp),
                        ),
                    minLines = 10,
                    // 本文もMonospaceで、コードエディタ感を演出
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                        color = IceTextPrimary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    ),
                    visualTransformation = MarkdownBoldVisualTransformation(
                        hideMarkers = true,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    // ガラスの表面色を適用
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = IceGlassSurface,
                        unfocusedContainerColor = IceGlassSurface,
                        disabledContainerColor = IceGlassSurface.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = IceCyan,
                    ),
                )
            }
        }
    }
}
