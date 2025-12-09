// app/src/main/java/com/example/bugmemo/ui/utils/GistContentBuilder.kt

package com.example.bugmemo.ui.utils

import com.example.bugmemo.data.Note
import com.example.bugmemo.data.remote.GistFileContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Gist同期用のコンテンツ（Markdownテキスト）を生成するビルダー
 * ViewModelから「文字列作成」の責務を分離するために使用
 */
object GistContentBuilder {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    /**
     * 単一のノートをGist送信用のMap形式に変換する
     * ファイル名は "bugmemo_{id}.md"
     */
    fun buildSingleFileMap(note: Note): Map<String, GistFileContent> {
        val filename = "bugmemo_${note.id}.md"
        // 単一同期の場合は、実行時点の時刻をLast Updatedとする（元の仕様を踏襲）
        val footerTime = formatTime(System.currentTimeMillis())
        val content = buildMarkdown(note, footerTime)
        return mapOf(filename to GistFileContent(content))
    }

    /**
     * 複数のノートをGist送信用のMap形式に変換する
     * ファイル名は "note_{id}.md"
     */
    fun buildBatchFileMap(notes: List<Note>): Map<String, GistFileContent> {
        return notes.associate { note ->
            val filename = "note_${note.id}.md"
            // 一括同期の場合は、ノートごとの最終更新日時を使用する（元の仕様を踏襲）
            val footerTime = formatTime(note.updatedAt)
            val content = buildMarkdown(note, footerTime)
            filename to GistFileContent(content)
        }
    }

    /**
     * 同期の説明文（Description）を生成
     */
    fun buildSyncDescription(isFullSync: Boolean, title: String? = null): String {
        val nowStr = formatTime(System.currentTimeMillis())
        return if (isFullSync) {
            "BugMemo Full Sync - $nowStr"
        } else {
            "BugMemo Note: ${title ?: "Untitled"}"
        }
    }

    // 内部ロジック: Markdown文字列の構築
    private fun buildMarkdown(note: Note, footerTimestamp: String): String {
        return buildString {
            appendLine("# ${note.title.ifBlank { "Untitled" }}")
            appendLine()
            appendLine(note.content)
            if (note.imagePaths.isNotEmpty()) {
                appendLine()
                appendLine("## Attachments")
                note.imagePaths.forEach { appendLine("- $it") }
            }
            appendLine()
            appendLine("> Last Updated: $footerTimestamp")
        }
    }

    // 内部ロジック: 日付フォーマット
    private fun formatTime(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}