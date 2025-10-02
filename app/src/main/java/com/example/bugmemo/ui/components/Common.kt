// app/src/main/java/com/example/bugmemo/ui/components/Common.kt
package com.example.bugmemo.ui.components

import androidx.compose.material3.*        // 必要に応じて明示 import に調整可
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note

// 例：ノートの所属フォルダ名を表示する小物
@Composable
fun NoteFolderLabel(
    note: Note,
    folders: List<Folder>,                 // ★ Added: 名前解決に必要
) {
    val folderName = folders.firstOrNull { it.id == note.folderId }?.name
        ?: "未選択"
    AssistChip(onClick = { /* no-op */ }, label = { Text(folderName) })
}

// 例：区切り線（旧 Divider の置換）
@Composable
fun SectionSeparator() {
    HorizontalDivider(thickness = 1.dp)    // ★ Changed: Divider → HorizontalDivider
}

// 例：メニューアンカー（ExposedDropdownMenu 用）
// 旧: Modifier.menuAnchor()（引数なし）は deprecated
@Composable
fun AnchorExample(modifier: Modifier = Modifier) {
    // もし使っていれば、次の形に置換してください（未使用ならこの関数ごと削除OK）
    // TextField(
    //     value = "...",
    //     onValueChange = { ... },
    //     modifier = modifier.menuAnchor(                      // ★ Changed
    //         type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
    //         enabled = true
    //     )
    // )
}
