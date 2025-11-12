// app/src/main/java/com/example/bugmemo/ui/common/MarkdownEditUtils.kt
package com.example.bugmemo.ui.common

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 選択範囲を **…** でトグル（既に囲まれていれば外す）
 */
fun toggleBold(tfv: TextFieldValue): TextFieldValue {
    val text = tfv.text
    val sel = tfv.selection
    val start = minOf(sel.start, sel.end).coerceIn(0, text.length)
    val end = maxOf(sel.start, sel.end).coerceIn(0, text.length)

    // 空選択は単語推定などの拡張もできるが、まずは無視
    if (start == end) return tfv

    val before = text.substring(0, start)
    val middle = text.substring(start, end)
    val after = text.substring(end)

    val hasBold = before.endsWith("**") && after.startsWith("**")
    return if (hasBold) {
        // ** を外す
        val newBefore = before.dropLast(2)
        val newAfter = after.drop(2)
        val newText = newBefore + middle + newAfter
        val newSelStart = newBefore.length
        val newSelEnd = newSelStart + middle.length
        tfv.copy(text = newText, selection = TextRange(newSelStart, newSelEnd))
    } else {
        // ** で囲む
        val newText = before + "**" + middle + "**" + after
        val newSelStart = (before.length + 2)
        val newSelEnd = newSelStart + middle.length
        tfv.copy(text = newText, selection = TextRange(newSelStart, newSelEnd))
    }
}
