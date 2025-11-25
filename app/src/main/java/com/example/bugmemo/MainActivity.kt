// app/src/main/java/com/example/bugmemo/MainActivity.kt
package com.example.bugmemo

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.bugmemo.ui.AppScaffold
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.theme.BugMemoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

// ★ Keep: 標準の拡張関数を使用(androidx.activity.viewModels)
// ★ Keep: これが必須(dagger.hilt.android.AndroidEntryPoint)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ★ Changed: Hilt が ViewModel を生成するため、Factory 指定は不要になりました！
    private val vm: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ステータスバーの透明化設定 (Iceberg Tech)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        enableStrictModeInDebug()
        seedDebugDataOnce()

        setContent {
            BugMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    AppScaffold(vm = vm)
                }
            }
        }
    }

    private fun enableStrictModeInDebug() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build(),
        )
    }

    private fun seedDebugDataOnce() {
        if (!BuildConfig.DEBUG) return
        val prefs = getSharedPreferences("debug_prefs", MODE_PRIVATE)
        val flagKey = "seed_done_v1"
        if (prefs.getBoolean(flagKey, false)) return

        lifecycleScope.launch {
            vm.addFolder("Inbox")
            vm.newNote()
            vm.setEditingTitle("サンプル: BugMemo へようこそ")
            vm.setEditingContent(
                """
                これはデバッグ用に自動投入されたサンプルノートです。
                - Tech-LuxuryなUIをお楽しみください
                - 下部ナビから「Search / Folders」を試せます
                """.trimIndent(),
            )
            vm.saveEditing()
            prefs.edit { putBoolean(flagKey, true) }
        }
    }
}
