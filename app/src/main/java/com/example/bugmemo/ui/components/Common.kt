// app/src/main/java/com/example/bugmemo/ui/components/Common.kt
package com.example.bugmemo.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note

/**
 * ノートの所属フォルダ名を小さなチップで表示
 */
@Composable
fun NoteFolderLabel(
    note: Note,
    folders: List<Folder>
) {
    val folderName = folders.firstOrNull { it.id == note.folderId }?.name ?: "未選択"
    AssistChip(onClick = { /* no-op */ }, label = { Text(folderName) })
}

/**
 * 区切り線（旧 Divider の置換）
 */
@Composable
fun SectionSeparator() {
    HorizontalDivider(thickness = 1.dp)
}

/**
 * メニューアンカー（ExposedDropdownMenu 用）の置換例
 *
 * 旧: Modifier.menuAnchor()（引数なし）は deprecated。
 * 実際に使う画面で、ExposedDropdownMenuAnchorType を指定する新オーバーロードに置換してください。
 */
@Composable
fun AnchorExample(
    modifier: Modifier = Modifier // ★ Changed: 規約どおり `modifier` に戻す
) {
    // ★ Added: 実際に modifier を使って「未使用」警告を消す
    //   ここでは簡易的に Text にそのまま渡しています。
    Text(text = "Anchor sample", modifier = modifier)

    // （実際の利用例／コメント）
    // TextField(
    //     value = "...",
    //     onValueChange = { ... },
    //     modifier = modifier.menuAnchor( // ★ 新しいオーバーロードを使う（使う画面で適用）
    //         type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
    //         enabled = true
    //     )
    // )
}

/* ─────────────── Preview（任意）────────────── */
@Preview(showBackground = true)
@Composable
private fun Preview_NoteFolderLabel() {
    val folders = remember { listOf(Folder(id = 1L, name = "Kotlin")) }
    val note = remember {
        Note(
            id = 1L,
            title = "サンプル",
            content = "内容",
            folderId = 1L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isStarred = false
        )
    }
    NoteFolderLabel(note = note, folders = folders)
}

@Preview(showBackground = true)
@Composable
private fun Preview_SectionSeparator() {
    SectionSeparator()
}

@Preview(showBackground = true)
@Composable
private fun Preview_AnchorExample() {
    AnchorExample()
}
