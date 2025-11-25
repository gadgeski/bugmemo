// app/src/main/java/com/example/bugmemo/ui/screens/MindMapScreen.kt
@file:Suppress("ktlint:standard:function-naming")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugmemo.ui.mindmap.MindMapViewModel
import com.example.bugmemo.ui.mindmap.MindNode
import com.example.bugmemo.ui.theme.IceCyan
import com.example.bugmemo.ui.theme.IceDeepNavy
import com.example.bugmemo.ui.theme.IceGlassBorder
import com.example.bugmemo.ui.theme.IceGlassSurface
import com.example.bugmemo.ui.theme.IceHorizon
import com.example.bugmemo.ui.theme.IceSilver
import com.example.bugmemo.ui.theme.IceSlate
import com.example.bugmemo.ui.theme.IceTextPrimary
import com.example.bugmemo.ui.theme.IceTextSecondary
import kotlinx.coroutines.launch

private val GUIDE_STROKE = 1.dp

/**
 * マインドマップ画面（Iceberg Tech Edition）
 * - 回路図のようなライン描画とガラスUI
 */
@Composable
fun MindMapScreen(
    onClose: () -> Unit = {},
    vm: MindMapViewModel = viewModel(),
) {
    val nodes by vm.nodes.collectAsStateWithLifecycle(initialValue = emptyList())
    val flat = remember(nodes) { vm.flatList() }
    var newTitle by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 背景: 深海グラデーション
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(IceHorizon, IceSlate, IceDeepNavy),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            // ステータスバーのインセットは中身で調整
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "MIND_MAP_VISUALIZER", // Tech表記
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = IceSilver,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = IceTextPrimary,
                        actionIconContentColor = IceSilver,
                    ),
                    modifier = Modifier.statusBarsPadding(),
                )
            },
        ) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Root Node Input (Glass Style) ──
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    TextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "NEW_ROOT_NODE",
                                color = IceTextSecondary.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val t = newTitle.trim()
                                if (t.isNotEmpty()) {
                                    vm.addRootNode(t)
                                    newTitle = ""
                                    scope.launch { snackbarHostState.showSnackbar("ROOT_NODE_ADDED") }
                                }
                            },
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IceGlassSurface,
                            unfocusedContainerColor = IceGlassSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = IceCyan,
                            focusedTextColor = IceTextPrimary,
                            unfocusedTextColor = IceTextSecondary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            vm.addRootNode(newTitle)
                            newTitle = ""
                            scope.launch { snackbarHostState.showSnackbar("ROOT_NODE_ADDED") }
                        },
                        enabled = newTitle.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyan,
                            contentColor = IceDeepNavy,
                            disabledContainerColor = IceGlassSurface.copy(alpha = 0.3f),
                            disabledContentColor = IceTextSecondary.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier.height(56.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }

                // ── Tree View ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    // 密度を高めて回路っぽく
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                    ),
                ) {
                    items(flat, key = { (n, _) -> n.id }) { (node, depth) ->
                        MindNodeRow(
                            node = node,
                            depth = depth,
                            onRename = { title ->
                                vm.renameNode(node.id, title)
                                scope.launch { snackbarHostState.showSnackbar("NODE_UPDATED") }
                            },
                            onDelete = {
                                vm.deleteNode(node.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "NODE_DELETED",
                                        actionLabel = if (vm.canUndoDelete()) "UNDO" else null,
                                        withDismissAction = true,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        vm.undoDelete()
                                    }
                                }
                            },
                            onAddChild = { title ->
                                vm.addChildNode(node.id, title)
                                scope.launch { snackbarHostState.showSnackbar("CHILD_NODE_ADDED") }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MindNodeRow(
    node: MindNode,
    depth: Int,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAddChild: (String) -> Unit,
) {
    var edit by remember { mutableStateOf(false) }
    var title by remember(node.id) { mutableStateOf(node.title) }

    var addingChild by remember { mutableStateOf(false) }
    var childTitle by remember { mutableStateOf("") }

    val childFocusRequester = remember { FocusRequester() }
    LaunchedEffect(addingChild) {
        if (addingChild) childFocusRequester.requestFocus()
    }

    // ★ Changed: 枝線の色をシアンにして回路図っぽく
    val guideColor = IceCyan.copy(alpha = 0.4f)

    val indentUnit = 20.dp
    // 少しインデントを広げる
    val armWidth = 16.dp
    val density = LocalDensity.current
    var anchorY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                val indentPx = with(density) { indentUnit.toPx() }
                val armPx = with(density) { armWidth.toPx() }
                val strokePx = with(density) { GUIDE_STROKE.toPx() }

                if (depth > 0) {
                    // 縦のガイド
                    for (i in 1..depth) {
                        val x = i * indentPx
                        drawLine(
                            color = guideColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = strokePx,
                        )
                    }
                    // 横の枝
                    val y = if (anchorY > 0f) anchorY.coerceIn(0f, size.height) else size.height / 2f
                    val x = depth * indentPx
                    drawLine(
                        color = guideColor,
                        start = Offset(x, y),
                        end = Offset(x + armPx, y),
                        strokeWidth = strokePx,
                    )

                    // ★ Added: 接合部のドット装飾
                    drawCircle(
                        color = IceCyan,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y),
                    )
                }
                drawContent()
            }
            .padding(start = indentUnit * depth + armWidth),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // ── Node Content Row ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                if (edit) {
                    // 編集モード
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(BorderStroke(1.dp, IceCyan), RoundedCornerShape(4.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val t = title.trim()
                                if (t.isNotEmpty()) {
                                    onRename(t)
                                    edit = false
                                }
                            },
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IceGlassSurface,
                            unfocusedContainerColor = IceGlassSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = IceCyan,
                            focusedTextColor = IceTextPrimary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                    IconButton(
                        onClick = {
                            onRename(title.trim())
                            edit = false
                        },
                        enabled = title.isNotBlank(),
                    ) { Icon(Icons.Filled.Save, contentDescription = "Save", tint = IceCyan) }
                } else {
                    // 表示モード
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = IceTextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layout ->
                            if (layout.lineCount > 0) {
                                val top = layout.getLineTop(0)
                                val bottom = layout.getLineBottom(0)
                                anchorY = (top + bottom) / 2f
                            }
                        },
                    )

                    // Action Buttons
                    IconButton(onClick = { edit = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = IceSilver.copy(alpha = 0.7f))
                    }
                    IconButton(
                        onClick = { addingChild = !addingChild },
                    ) {
                        Icon(
                            // トグル状態でアイコン色を変える
                            Icons.Filled.SubdirectoryArrowRight,
                            contentDescription = "Add Child",
                            tint = if (addingChild) IceCyan else IceSilver.copy(alpha = 0.7f),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = IceSilver.copy(alpha = 0.5f))
                    }
                }
            }

            // ── Add Child Row (Collapsible) ──
            if (addingChild) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp), // 少しずらす
                ) {
                    // ガイド線装飾（子ノードへの接続線）
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(IceCyan.copy(alpha = 0.3f)),
                    )

                    TextField(
                        value = childTitle,
                        onValueChange = { childTitle = it },
                        singleLine = true,
                        placeholder = { Text("CHILD_NODE_NAME", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(childFocusRequester)
                            .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(4.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val t = childTitle.trim()
                                if (t.isNotEmpty()) {
                                    onAddChild(t)
                                    childTitle = ""
                                    addingChild = false
                                }
                            },
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IceGlassSurface,
                            unfocusedContainerColor = IceGlassSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = IceCyan,
                            focusedTextColor = IceTextPrimary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )

                    Button(
                        onClick = {
                            val t = childTitle.trim()
                            if (t.isNotEmpty()) {
                                onAddChild(t)
                                childTitle = ""
                                addingChild = false
                            }
                        },
                        enabled = childTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = IceCyan, contentColor = IceDeepNavy),
                        shape = RoundedCornerShape(4.dp),
                    ) { Text("ADD") }
                }
            }
        }
    }
}
