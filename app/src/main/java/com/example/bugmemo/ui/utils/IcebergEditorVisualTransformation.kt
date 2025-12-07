// app/src/main/java/com/example/bugmemo/ui/utils/IcebergEditorVisualTransformation.kt
package com.example.bugmemo.ui.utils
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.bugmemo.ui.theme.IceCyan
import com.example.bugmemo.ui.theme.IceGlassSurface
import com.example.bugmemo.ui.theme.IceTextPrimary

/**
 * Iceberg Tech エディタ用ビジュアルトランスフォーメーション
 * 入力中のMarkdownテキストに対し、リアルタイムでハイライト（装飾）を適用する。
 *
 * 対応記法:
 * - コードブロック(```...```)
 * - インラインコード (`...`): 等幅フォント + 背景色
 * - 太字 (**...**): 太字 + シアン発光
 * - 見出し (# ...): 文字サイズ大 + 太字 + シアン
 */
class IcebergEditorVisualTransformation : VisualTransformation {

    // 正規表現（Kotlinの命名規則に従いキャメルケースに変更）
    private val codeBlockRegex = Regex("```([\\s\\S]*?)```")
    private val inlineCodeRegex = Regex("`([^`]+)`")
    private val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    private val headingRegex = Regex("^(#{1,6})\\s+(.*)$", RegexOption.MULTILINE)

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val builder = AnnotatedString.Builder(rawText)

        // 1. Code Block (```...```)
        // 最も優先度が高い（他の装飾を飲み込むため）
        codeBlockRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.5f),
                    // 少し濃いガラス背景
                    color = IceTextPrimary,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // 2. Inline Code (`...`)
        inlineCodeRegex.findAll(rawText).forEach { match ->
            // コードブロック内にある場合はスキップしたいが、簡易実装として上書き適用
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.5f),
                    color = IceCyan,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // 3. Heading (# Title)
        headingRegex.findAll(rawText).forEach { match ->
            // 行全体を強調
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = IceCyan,
                    fontSize = 18.sp,
                    // 少し大きく
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // 4. Bold (**...**)
        boldRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = IceCyan,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // OffsetMapping.Identity を返すことで、
        // 「文字数は変えずに、色とフォントだけ変える」挙動にする。
        // これにより、カーソル位置ズレのバグを回避しつつリッチな見た目を実現。
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
