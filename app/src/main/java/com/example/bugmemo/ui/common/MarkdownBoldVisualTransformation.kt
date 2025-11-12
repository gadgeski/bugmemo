// app/src/main/java/com/example/bugmemo/ui/common/MarkdownBoldVisualTransformation.kt
package com.example.bugmemo.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// ★ Removed: 未使用 import（LocaleList / TextDirection）

/**
 * **…** / __…__ を太字スタイルで描く（記号は表示したまま or 非表示を選択）
 */
class MarkdownBoldVisualTransformation(
    private val hideMarkers: Boolean = false,
    // true: ** を非表示にする（簡易マッピング）
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val src = text.text
        val boldRanges = findBoldRanges(src)

        return if (!hideMarkers) {
            // 記号はそのまま表示。内側だけ太字（安全）
            val out = AnnotatedString.Builder(src)
            // ★ Changed: 不要な destructuring で発生していた「Unused variable」を回避
            //            （markerLen を使わないので Range をそのまま参照）
            for (r in boldRanges) {
                // r.start, r.end は記号を除いた内側範囲
                out.addStyle(SpanStyle(fontWeight = FontWeight.Bold), r.start, r.end)
            }
            TransformedText(
                text = out.toAnnotatedString(),
                offsetMapping = OffsetMapping.Identity,
            )
        } else {
            // 記号を取り除いて見せる（簡易版オフセットマッピング）
            val out = AnnotatedString.Builder()
            val mapping = IntArray(src.length + 1) { it } // transformed -> original
            var outIdx = 0
            var i = 0
            val rangesIter = boldRanges.iterator()
            var current = if (rangesIter.hasNext()) rangesIter.next() else null

            while (i < src.length) {
                val isMarker = current != null &&
                    (
                        i in (current.start - current.markerLen) until current.start ||
                            i in current.end until (current.end + current.markerLen)
                        )
                if (isMarker) {
                    // スキップ（表示しない）
                    i++
                    continue
                }

                // 1文字ずつコピー
                val ch = src[i].toString()
                val startOut = outIdx
                out.append(ch)
                val endOut = ++outIdx
                mapping[endOut] = i + 1

                // 太字範囲ならスタイル付与
                val inBold = current != null && i in current.start until current.end
                if (inBold) {
                    out.addStyle(SpanStyle(fontWeight = FontWeight.Bold), startOut, endOut)
                }

                i++

                // 範囲を通過し終えたら次へ
                if (current != null && i >= current.end + current.markerLen) {
                    current = if (rangesIter.hasNext()) rangesIter.next() else null
                }
            }

            val om = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    // ざっくり：元の offset までに存在するマーカー総数ぶんを引く
                    var markersBefore = 0
                    for (r in boldRanges) {
                        if (offset >= r.start) markersBefore += r.markerLen
                        // 開き記号
                        if (offset >= r.end) markersBefore += r.markerLen
                        // 閉じ記号
                    }
                    val t = (offset - markersBefore).coerceAtLeast(0)
                    return t.coerceAtMost(out.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    // 近似マッピング（前段の mapping を利用）
                    return mapping[offset.coerceIn(0, mapping.lastIndex)]
                }
            }

            TransformedText(
                text = out.toAnnotatedString(),
                offsetMapping = om,
            )
        }
    }

    private data class Range(val start: Int, val end: Int, val markerLen: Int)

    // **…** と __…__ の内側範囲を抽出（入れ子/未クローズは簡易に無視）
    private fun findBoldRanges(text: String): List<Range> {
        val res = mutableListOf<Range>()
        var i = 0
        while (i < text.length) {
            val s1 = text.indexOf("**", i)
            val s2 = text.indexOf("__", i)
            val s = listOf(s1, s2).filter { it >= 0 }.minOrNull() ?: break
            val marker = if (s == s1) "**" else "__"
            val e = text.indexOf(marker, s + 2)
            if (e < 0) break
            res += Range(start = s + 2, end = e, markerLen = 2)
            i = e + 2
        }
        return res
    }
}
