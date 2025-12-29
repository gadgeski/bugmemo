// app/src/main/java/com/gadgeski/bugmemo/ui/screens/NoteEditorScreen.kt

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gadgeski.bugmemo.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gadgeski.bugmemo.R
import com.gadgeski.bugmemo.core.AppLocaleManager
import com.gadgeski.bugmemo.ui.NotesViewModel
import com.gadgeski.bugmemo.ui.theme.IceCyan
import com.gadgeski.bugmemo.ui.theme.IceDeepNavy
import com.gadgeski.bugmemo.ui.theme.IceGlassBorder
import com.gadgeski.bugmemo.ui.theme.IceGlassSurface
import com.gadgeski.bugmemo.ui.theme.IceHorizon
import com.gadgeski.bugmemo.ui.theme.IceSilver
import com.gadgeski.bugmemo.ui.theme.IceSlate
import com.gadgeski.bugmemo.ui.theme.IceTextPrimary
import com.gadgeski.bugmemo.ui.theme.IceTextSecondary
import com.gadgeski.bugmemo.ui.utils.IcebergEditorVisualTransformation
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File
import java.util.UUID

// ★変更: TabRow -> PrimaryTabRow(androidx.compose.material3.PrimaryTabRow)

private enum class EditorTabMode { Edit, Preview }

@OptIn(FlowPreview::class)
@Composable
fun NoteEditorScreen(
    vm: NotesViewModel,
    onBack: () -> Unit = {},
) {
    val editing by vm.editing.collectAsStateWithLifecycle(initialValue = null)
    // 警告 "Condition 'enabled' is always true" が出る場合、editingがnullにならない型推論がされている可能性があります。
    // ロジック的には安全のため != null チェックを残しても問題ありませんが、
    // ここでは警告を抑制するため、また enabled 変数はUI制御で使われているためそのままにします。
    val enabled = editing != null

    val context = LocalContext.current
    val fontScale by AppLocaleManager.editorFontScaleFlow(context)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    // IDE誤検知回避：by委譲を使わずStateオブジェクトを保持
    val showDeleteDialogState = remember { mutableStateOf(false) }

    // タブ状態
    val tabModeState = remember { mutableStateOf(EditorTabMode.Edit) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(tabModeState.value) {
        if (tabModeState.value == EditorTabMode.Preview) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    // IDE誤検知回避：Stateオブジェクト保持
    val contentFieldState = remember(editing?.id) {
        mutableStateOf(
            TextFieldValue(
                text = editing?.content.orEmpty(),
                selection = TextRange(editing?.content?.length ?: 0),
            ),
        )
    }

    // Debounce（300ms）
    LaunchedEffect(editing?.id) {
        snapshotFlow { contentFieldState.value.text }
            .debounce(300L)
            .distinctUntilChanged()
            .collect { text ->
                if (enabled && text != editing?.content) {
                    vm.setEditingContent(text)
                }
            }
    }

    // DBからの同期
    LaunchedEffect(editing?.content) {
        val currentText = contentFieldState.value.text
        val newText = editing?.content.orEmpty()
        if (currentText != newText) {
            contentFieldState.value = contentFieldState.value.copy(
                text = newText,
                selection = TextRange(newText.length),
            )
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val stream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            stream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            vm.addImagePath(file.absolutePath)
        }
    }

    val backgroundBrush = remember {
        Brush.verticalGradient(colors = listOf(IceHorizon, IceSlate, IceDeepNavy))
    }

    if (showDeleteDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogState.value = false },
            containerColor = IceSlate,
            titleContentColor = IceTextPrimary,
            textContentColor = IceTextSecondary,
            title = { Text("Delete Note?") },
            text = { Text("このメモを削除しますか？\n（削除後は元に戻す操作が可能です）") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteEditing()
                        showDeleteDialogState.value = false
                        onBack()
                    },
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogState.value = false }) {
                    Text("Cancel", color = IceSilver)
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            val titleText = editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                                ?: stringResource(R.string.title_new_note)
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = IceTextPrimary,
                            actionIconContentColor = IceSilver,
                            navigationIconContentColor = IceCyan,
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
                            IconButton(
                                enabled = enabled,
                                onClick = { showDeleteDialogState.value = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Note",
                                    tint = if (enabled) IceSilver else IceSilver.copy(alpha = 0.3f),
                                )
                            }

                            IconButton(
                                enabled = enabled,
                                onClick = { vm.syncCurrentNoteToGist() },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudUpload,
                                    contentDescription = "Sync to Gist",
                                    tint = if (editing?.gistId != null) IceCyan else IceSilver,
                                )
                            }

                            IconButton(onClick = { vm.saveEditing() }, enabled = enabled) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = stringResource(R.string.cd_save),
                                    tint = if (enabled) IceCyan else IceSilver.copy(alpha = 0.5f),
                                )
                            }
                        },
                    )

                    // --- Edit / Preview タブ ---
                    val selectedIndex = if (tabModeState.value == EditorTabMode.Edit) 0 else 1

                    // ★FIX: TabRow -> PrimaryTabRow に変更
                    PrimaryTabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = Color.Transparent,
                        contentColor = IceCyan,
                        // dividerはデフォルトで適切に処理されるため削除、indicatorもデフォルトを使用
                    ) {
                        Tab(
                            selected = selectedIndex == 0,
                            onClick = { tabModeState.value = EditorTabMode.Edit },
                            text = { Text("Edit", color = IceTextPrimary) },
                        )
                        Tab(
                            selected = selectedIndex == 1,
                            onClick = { tabModeState.value = EditorTabMode.Preview },
                            text = { Text("Preview", color = IceTextPrimary) },
                        )
                    }
                }
            },
            bottomBar = {
                if (tabModeState.value == EditorTabMode.Edit) {
                    EditorBottomBar(
                        enabled = enabled,
                        contentField = contentFieldState.value,
                        onContentChange = { newField ->
                            contentFieldState.value = newField
                        },
                        onImagePick = {
                            launcher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                }
            },
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    // .verticalScroll(rememberScrollState()) ← previewでもスクロール出来る様に外す。
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Title
                if (tabModeState.value == EditorTabMode.Edit) {
                    TextField(
                        value = editing?.title.orEmpty(),
                        onValueChange = { text -> vm.setEditingTitle(text) },
                        placeholder = { Text(stringResource(R.string.label_title), color = IceTextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
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
                        ),
                    )
                } else {
                    Text(
                        text = (
                            editing?.title?.ifBlank { stringResource(R.string.label_untitled) }
                                ?: stringResource(R.string.title_new_note)
                            ),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize * fontScale,
                            color = IceCyan,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Images
                if (!editing?.imagePaths.isNullOrEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        items(editing!!.imagePaths) { path ->
                            Box {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                IconButton(
                                    // ★ここは “Preview中は編集不可” だけにする
                                    enabled = tabModeState.value == EditorTabMode.Edit,
                                    onClick = { vm.removeImagePath(path) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(IceDeepNavy.copy(alpha = 0.5f), CircleShape),
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        null,
                                        tint = IceTextPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Content
                if (tabModeState.value == EditorTabMode.Edit) {
                    TextField(
                        value = contentFieldState.value,
                        onValueChange = { v -> contentFieldState.value = v },
                        placeholder = { Text(stringResource(R.string.label_content), color = IceTextSecondary.copy(alpha = 0.5f)) },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = 300.dp)
                            .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(12.dp)),
                        minLines = 10,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                            lineHeight = (MaterialTheme.typography.bodyLarge.fontSize * fontScale) * 1.5,
                            color = IceTextPrimary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                        visualTransformation = remember { IcebergEditorVisualTransformation() },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IceGlassSurface,
                            unfocusedContainerColor = IceGlassSurface,
                            disabledContainerColor = IceGlassSurface.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = IceCyan,
                        ),
                    )
                } else {
                    // Preview：Markdownレンダリング
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            // .heightIn(min = 300.dp) ← previewでもスクロール出来る様に外す。
                            .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(12.dp))
                            .verticalScroll(rememberScrollState())
                            // ★previewでもスクロール出来る様に追加！
                            .background(IceGlassSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        MarkdownText(
                            markdown = contentFieldState.value.text,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                                lineHeight = (MaterialTheme.typography.bodyLarge.fontSize * fontScale) * 1.5,
                                color = IceTextPrimary,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            ),
                        )
                    }
                }
            }
        }
    }
}

// ───── EditorBottomBar（変更なし） ─────
@Composable
private fun EditorBottomBar(
    enabled: Boolean,
    contentField: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit,
    onImagePick: () -> Unit,
) {
    val colorMenu = remember { mutableStateOf(false) }
    val headingMenu = remember { mutableStateOf(false) }

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
        onContentChange(contentField.copy(text = newText, selection = newSel))
    }

    BottomAppBar(
        containerColor = IceSlate.copy(alpha = 0.9f),
        contentColor = IceCyan,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
    ) {
        IconButton(
            enabled = enabled,
            onClick = {
                if (!enabled) return@IconButton
                val text = contentField.text
                val sel = contentField.selection

                val newValue = if (!sel.collapsed) {
                    val selected = text.substring(sel.start, sel.end)
                    if (selected.startsWith("**") && selected.endsWith("**") && selected.length >= 4) {
                        val inner = selected.removePrefix("**").removeSuffix("**")
                        val newText = text.take(sel.start) + inner + text.drop(sel.end)
                        TextFieldValue(newText, TextRange(sel.start + inner.length))
                    } else {
                        val newText = text.take(sel.start) + "**" + selected + "**" + text.drop(sel.end)
                        TextFieldValue(newText, TextRange(sel.end + 4))
                    }
                } else {
                    val newText = text.take(sel.start) + "****" + text.drop(sel.start)
                    TextFieldValue(newText, TextRange(sel.start + 2))
                }
                onContentChange(newValue)
            },
        ) { Icon(Icons.Filled.FormatBold, null) }

        IconButton(
            enabled = enabled,
            onClick = { onContentChange(wrapOrUnwrap(contentField, "[u]", "[/u]")) },
        ) { Icon(Icons.Filled.FormatUnderlined, null) }

        Box {
            IconButton(enabled = enabled, onClick = { colorMenu.value = true }) {
                Icon(Icons.Filled.FormatColorText, null)
            }
            DropdownMenu(
                expanded = colorMenu.value,
                onDismissRequest = { colorMenu.value = false },
                containerColor = IceSlate,
                border = BorderStroke(1.dp, IceGlassBorder),
            ) {
                listOf("#ff5252", "#ff9800", "#4caf50", "#2196f3").forEach { hex ->
                    DropdownMenuItem(
                        text = { Text(hex, color = IceTextPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                        onClick = {
                            onContentChange(wrapOrUnwrap(contentField, "[color=$hex]", "[/color]"))
                            colorMenu.value = false
                        },
                    )
                }
            }
        }

        Box {
            IconButton(enabled = enabled, onClick = { headingMenu.value = true }) {
                Icon(Icons.Filled.Title, null)
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
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onImagePick, enabled = enabled) {
            Icon(Icons.Filled.AttachFile, "Attach Image")
        }
    }
}
