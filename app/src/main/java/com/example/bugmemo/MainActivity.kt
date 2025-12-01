// app/src/main/java/com/example/bugmemo/MainActivity.kt
package com.example.bugmemo

import android.content.Intent
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        enableStrictModeInDebug()
        seedDebugDataOnce()

        // ★ Fix: 画面回転時などに再度Intentを処理しないよう、初回起動時のみチェック
        if (savedInstanceState == null) {
            handleIntent(intent)
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // ★ Fix: 新しいIntentをセットしておく（Androidの作法）
        this.intent = intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // ★ Fix: mimeType のチェックを "text/plain" 完全一致から "text/" 始まりに緩和
        // これで text/plain; charset=utf-8 なども許容される
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                vm.handleSharedText(sharedText)
            }
        }
    }

    private fun enableStrictModeInDebug() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build())
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
