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
import com.example.bugmemo.ui.theme.IceTextSecondary

// ★追加: これが抜けていました(com.example.bugmemo.ui.theme.IceTextSecondary)

class IcebergEditorVisualTransformation : VisualTransformation {

    private val codeBlockRegex = Regex("```([\\s\\S]*?)```")
    private val inlineCodeRegex = Regex("`([^`]+)`")
    private val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    private val headingRegex = Regex("^(#{1,6})\\s+(.*)$", RegexOption.MULTILINE)

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val builder = AnnotatedString.Builder(rawText)

        // 1. Code Block (```...```)
        codeBlockRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.3f),
                    // ★FIX: ここをPrimaryからSecondaryに変更し、コードっぽさを出す
                    color = IceTextSecondary,
                    fontSize = 14.sp,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // 2. Inline Code (`...`)
        inlineCodeRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.3f),
                    color = IceCyan,
                    fontWeight = FontWeight.Bold,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // 3. Heading (# Title)
        headingRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = IceCyan,
                    fontSize = 20.sp,
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

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
