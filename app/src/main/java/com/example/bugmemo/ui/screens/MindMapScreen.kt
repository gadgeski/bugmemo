// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/screens/MindMapScreen.kt
// ----------------------------------------
package com.example.bugmemo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Note

@Composable
fun MindMapScreen(notes: List<Note>) {
    val items = notes.take(12)
    Box(Modifier.fillMaxSize().padding(12.dp)) {
        Card(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().padding(12.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (items.isNotEmpty()) {
                        val cols = 3
                        val cellW = size.width / cols
                        val rows = (items.size + cols - 1) / cols
                        val cellH = size.height / rows
                        items.forEachIndexed { idx, _ ->
                            val c = idx % cols
                            val r = idx / cols
                            val cx = c * cellW + cellW / 2
                            val cy = r * cellH + cellH / 2
                            drawCircle(
                                color = Color(0xFFBBDEFB),
                                radius = minOf(cellW, cellH) * 0.28f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
                Column {
                    Text("Mind Map (demo)", modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.titleMedium)
                    Text("後でドラッグ/リンクを追加できます", modifier = Modifier.padding(horizontal = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
