// app/src/main/java/com/example/bugmemo/ui/common/MarkdownText.kt
@file:Suppress("FunctionName")
// ★ Compose慣習(PascalCase)を許容

package com.example.bugmemo.ui.common

// ★ 必要なimport一式
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val annotated = remember(text) { boldOnlyToAnnotated(text) }
    Text(text = annotated, modifier = modifier, style = style, maxLines = maxLines, overflow = overflow)
}

// **bold** / __bold__ だけ太字にする簡易版
private fun boldOnlyToAnnotated(src: String): AnnotatedString {
    val b = AnnotatedString.Builder()
    var i = 0
    while (i < src.length) {
        val s1 = src.indexOf("**", i)
        val s2 = src.indexOf("__", i)
        val candidates = listOf(s1, s2).filter { it >= 0 }
        if (candidates.isEmpty()) {
            b.append(src.substring(i))
            break
        }
        val s = candidates.minOrNull()!!
        b.append(src.substring(i, s))
        val marker = if (s == s1) "**" else "__"
        val e = src.indexOf(marker, s + marker.length)
        if (e < 0) {
            b.append(src.substring(s))
            break
        }
        b.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        b.append(src.substring(s + marker.length, e))
        b.pop()
        i = e + marker.length
    }
    return b.toAnnotatedString()
}
