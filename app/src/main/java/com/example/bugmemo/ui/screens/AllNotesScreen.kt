// app/src/main/java/com/example/bugmemo/ui/screens/AllNotesScreen.kt

@file:Suppress("ktlint:standard:function-naming")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.bugmemo.data.Note
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.theme.IceCyan
import com.example.bugmemo.ui.theme.IceDeepNavy
import com.example.bugmemo.ui.theme.IceGlassBorder
import com.example.bugmemo.ui.theme.IceGlassSurface
import com.example.bugmemo.ui.theme.IceHorizon
import com.example.bugmemo.ui.theme.IceSilver
import com.example.bugmemo.ui.theme.IceSlate
import com.example.bugmemo.ui.theme.IceTextPrimary
import com.example.bugmemo.ui.theme.IceTextSecondary

// ★ Added: 押下状態の検知(androidx.compose.foundation.interaction.collectIsPressedAsState)
// ★ Changed: ワイルドカードインポートをやめ、個別インポートに変更(com.example.bugmemo.ui.theme.IceCyanなど)

/* ========================================================
   BugMemo Screen: "Iceberg Tech" Edition (With Glow Effect)
   ======================================================== */

@Composable
fun AllNotesScreen(
    onOpenEditor: () -> Unit = {},
    vm: NotesViewModel,
) {
    val notesPaging: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

    // 背景: 深海へのダイブ
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                IceHorizon,
                IceSlate,
                IceDeepNavy,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        vm.newNote()
                        onOpenEditor()
                    },
                    containerColor = IceCyan,
                    contentColor = IceDeepNavy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Note")
                }
            },
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
            ) {
                // ──── Header Section ────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "BUG\nTRACKER",
                        style = MaterialTheme.typography.displayLarge,
                        color = IceTextPrimary.copy(alpha = 0.8f),
                        lineHeight = 56.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "// SYSTEM_LOGS_V2.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceCyan,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ──── List Section ────
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        bottom = 100.dp,
                    ),
                ) {
                    items(
                        count = notesPaging.itemCount,
                        key = { index -> notesPaging[index]?.id ?: index },
                    ) { index ->
                        val note = notesPaging[index]
                        if (note != null) {
                            TechGlassCard(
                                note = note,
                                onClick = {
                                    vm.loadNote(note.id)
                                    onOpenEditor()
                                },
                                onToggleStar = {
                                    vm.toggleStar(note.id, note.isStarred)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechGlassCard(
    note: Note,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    // ★ Fix: マイクロインタラクションの強化（発光アニメーション）
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 押している間、枠線が IceCyan に光る
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isPressed) IceCyan else IceGlassBorder,
        label = "borderGlow",
        animationSpec = tween(durationMillis = 150),
    )

    // 押している間、背景も少し明るくする
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed) IceGlassSurface.copy(alpha = 0.25f) else IceGlassSurface,
        label = "containerGlow",
        animationSpec = tween(durationMillis = 150),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = IceCyan),
                // リップルも維持
                onClick = onClick,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
            // ★ アニメーション適用
        ),
        border = BorderStroke(1.dp, animatedBorderColor),
        // ★ アニメーション適用
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "UNTITLED_LOG" },
                    style = MaterialTheme.typography.titleMedium,
                    color = IceTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IceTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Starred",
                        tint = IceCyan,
                    )
                } else {
                    Icon(
                        Icons.Outlined.StarBorder,
                        contentDescription = "Not starred",
                        tint = IceSilver,
                    )
                }
            }
        }
    }
}
