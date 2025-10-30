// app/src/main/java/com/example/bugmemo/MainActivity.kt
package com.example.bugmemo

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.bugmemo.ui.AppScaffold
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.theme.AppTheme
import kotlinx.coroutines.launch

// ★ Added: (androidx.core.content.edit) SharedPreferences KTX 拡張（prefs.edit { ... } 用）
// ★ keep: StrictMode 利用のため(android.os.StrictMode)の import は上で追加済み
// ★ keep: Activity のスコープでシードを非同期実行(androidx.lifecycle.lifecycleScope)
// ★ keep: コルーチン起動(kotlinx.coroutines.launch)

class MainActivity : ComponentActivity() {

    // ★ keep: Companion の Factory をそのまま使う（DB と DataStore を内部で解決）
    private val vm: NotesViewModel by viewModels { NotesViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ keep: Debug のみ StrictMode を有効化（ログ出力）
        enableStrictModeInDebug()

        // ★ keep: Debug のみシードを一度だけ投入（重複投入を防止）
        seedDebugDataOnce()

        setContent {
            AppTheme(
                useDynamicColor = false,
                // ★ keep: 端末依存を避ける
            ) {
                // ★ keep: 明示的に VM を渡す（AppScaffold 側で viewModel() は呼ばない）
                AppScaffold(vm = vm)
            }
        }
    }

    // ★ keep: Debug ビルド時に StrictMode を有効化（致命ペナルティは付けずログのみ）
    private fun enableStrictModeInDebug() {
        if (!BuildConfig.DEBUG) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                // ネットワークやディスク I/O を検出
                .penaltyLog()
                // Logcat に警告を出す
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                // Close されてないリソース等
                .detectActivityLeaks()
                .penaltyLog()
                .build(),
        )
        // ※ penaltyDeathOnNetwork などは開発効率を下げるため敢えて付けていません
    }

    // ★ keep: デバッグ用のシードを“1回だけ”投入
    // - SharedPreferences のフラグでリピート投入を防止
    // - Repository 操作は NotesViewModel 経由で行う（内部でコルーチン起動）
    private fun seedDebugDataOnce() {
        if (!BuildConfig.DEBUG) return

        val prefs = getSharedPreferences("debug_prefs", MODE_PRIVATE)
        val flagKey = "seed_done_v1"
        if (prefs.getBoolean(flagKey, false)) return

        lifecycleScope.launch {
            // フォルダ 1 件
            vm.addFolder("Inbox")

            // ノート 1 件（Welcome ノート）
            vm.newNote()
            vm.setEditingTitle("サンプル: BugMemo へようこそ")
            vm.setEditingContent(
                """
                これはデバッグ用に自動投入されたサンプルノートです。
                - 下部ナビから「Search / Folders」を試せます
                - 右上のショートカットで All Notes へ遷移できます
                - エディタで保存 / 削除 → Undo も確認してみてください
                """.trimIndent(),
            )
            vm.saveEditing()

            // ★ Changed: KTX 拡張を使用して簡潔にフラグ保存（lint警告を解消）
            prefs.edit {
                putBoolean(flagKey, true)
            }
        }
    }
}
