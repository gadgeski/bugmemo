// app/src/main/java/com/example/bugmemo/ui/common/MarkdownBoldVisualTransformation.kt
package com.example.bugmemo.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * 行頭の見出し (# / ## / ###) を装飾して描画する。
 * hideMarkers=true のとき、記号は非表示。
 */
class MarkdownBoldVisualTransformation(
    private val hideMarkers: Boolean = false,
    // true: 記号を非表示
) : VisualTransformation {

    // 可変長のマーカーに対応するレンジ情報
    private data class Mark(
        val start: Int,
        // 内容の開始（記号の内側, inclusive）
        val end: Int,
        // 内容の終端（exclusive）
        val prefixLen: Int,
        // 開き記号の長さ
        val suffixLen: Int,
        // 閉じ記号の長さ
        val style: SpanStyle,
        // 適用スタイル
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val src = text.text

        // すべての装飾レンジを集約
        val marks = buildList {
            addAll(findBoldMarks(src))
            addAll(findUnderlineMarks(src))
            addAll(findColorMarks(src))
            addAll(findHeadingMarks(src))
        }.sortedBy { it.start }
        // ★ Changed: 前から順に処理しやすく

        if (!hideMarkers) {
            // 記号はそのまま表示。内容範囲にだけスタイルを当てる
            val out = AnnotatedString.Builder(src)
            for (m in marks) out.addStyle(m.style, m.start, m.end)
            return TransformedText(out.toAnnotatedString(), OffsetMapping.Identity)
        }

        // ★ Changed: 記号を非表示にしてレンダリング（マッピングを構築）
        val out = AnnotatedString.Builder()
        val mapping = IntArray(src.length + 1) { it }
        // transformed -> original
        var outIdx = 0
        var i = 0
        while (i < src.length) {
            val isMarker = marks.any { m ->
                i in (m.start - m.prefixLen) until m.start ||
                    i in m.end until (m.end + m.suffixLen)
            }
            if (isMarker) {
                i++
                continue
                // ★ keep: 記号をスキップ
            }
            val startOut = outIdx
            out.append(src[i].toString())
            // ★ Changed: 一時変数を作らずインライン化（inlined）
            outIdx++
            mapping[outIdx] = i + 1

            // その位置にかかる全スタイルを付与（重ねがけOK）
            marks.forEach { m ->
                if (i in m.start until m.end) out.addStyle(m.style, startOut, outIdx)
            }
            i++
        }

        val om = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 可変長の prefix/suffix 分を差し引く
                var delta = 0
                for (m in marks) {
                    if (offset >= m.start) delta += m.prefixLen
                    if (offset >= m.end) delta += m.suffixLen
                }
                val t = (offset - delta).coerceAtLeast(0)
                return t.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int = mapping[offset.coerceIn(0, mapping.lastIndex)]
        }
        return TransformedText(out.toAnnotatedString(), om)
    }

    // ─────────── 検出ロジック ───────────

    // **…** と __…__
    private fun findBoldMarks(text: String): List<Mark> {
        val res = mutableListOf<Mark>()
        res += findPairs(text, open = "**", close = "**", style = SpanStyle(fontWeight = FontWeight.Bold))
        res += findPairs(text, open = "__", close = "__", style = SpanStyle(fontWeight = FontWeight.Bold))
        return res
    }

    // [u]…[/u]
    private fun findUnderlineMarks(text: String): List<Mark> = findPairs(text, "[u]", "[/u]", SpanStyle(textDecoration = TextDecoration.Underline))

    // [color=#RRGGBB]…[/color]
    private fun findColorMarks(text: String): List<Mark> {
        val res = mutableListOf<Mark>()
        var idx = 0
        while (true) {
            val openIdx = text.indexOf("[color=", idx)
            if (openIdx < 0) break
            val closeBracket = text.indexOf(']', openIdx + 7)
            if (closeBracket < 0) break

            val tag = text.substring(openIdx + 7, closeBracket)
            // ★ keep: substring(a,b) は take/drop 置換対象外
            val color = parseHexColorOrNull(tag)
            val endTag = "[/color]"
            val endIdx = text.indexOf(endTag, closeBracket + 1)

            if (color != null && endIdx >= 0) {
                res += Mark(
                    start = closeBracket + 1,
                    end = endIdx,
                    prefixLen = (closeBracket + 1) - openIdx,
                    suffixLen = endTag.length,
                    style = SpanStyle(color = color),
                )
                idx = endIdx + endTag.length
            } else {
                idx = closeBracket + 1
            }
        }
        return res
    }

    // 見出し: 行頭の # / ## / ### + 半角スペース
    private fun findHeadingMarks(text: String): List<Mark> {
        val res = mutableListOf<Mark>()
        var lineStart = 0
        // ★ keep: 行走査に必要
        while (lineStart <= text.length) {
            val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val m = Regex("^(#{1,3})\\s+(.*)$").find(line)
            if (m != null) {
                val level = m.groupValues[1].length
                val contentStartInLine = level + 1
                // "# " の分
                val start = lineStart + contentStartInLine
                // val end = lineEnd // ★ Removed: Variable can be inlined
                val style = when (level) {
                    1 -> SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    2 -> SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    else -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                res += Mark(
                    start = start,
                    end = lineEnd,
                    // ★ Changed: inline して警告解消
                    prefixLen = contentStartInLine,
                    suffixLen = 0,
                    style = style,
                )
            }
            if (lineEnd == text.length) break
            lineStart = lineEnd + 1
        }
        return res
    }

    // 共通：open/close で囲むペアを順スキャン（入れ子は簡易に非対応）
    private fun findPairs(text: String, open: String, close: String, style: SpanStyle): List<Mark> {
        val res = mutableListOf<Mark>()
        var idx = 0
        while (true) {
            val s = text.indexOf(open, idx)
            if (s < 0) break
            val e = text.indexOf(close, s + open.length)
            if (e < 0) break
            res += Mark(
                start = s + open.length,
                end = e,
                prefixLen = open.length,
                suffixLen = close.length,
                style = style,
            )
            idx = e + close.length
        }
        return res
    }

    // "#xxxxxx" or "xxxxxx" → Color
    private fun parseHexColorOrNull(tag: String): Color? {
        val hex = tag.removePrefix("#")
        if (hex.length != 6 || hex.any { it !in "0123456789abcdefABCDEF" }) return null
        // ★ keep: lint 推奨（2桁ずつ分割）
        val parts = hex.chunked(2)
        if (parts.size != 3) return null
        val r = parts[0].toInt(16)
        val g = parts[1].toInt(16)
        val b = parts[2].toInt(16)
        return Color(red = r / 255f, green = g / 255f, blue = b / 255f)
    }
}
