// app/src/main/java/com/example/bugmemo/MainActivity.kt
package com.example.bugmemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.bugmemo.ui.AppScaffold
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    // ★ Changed: Companion の Factory をそのまま使う（DB と DataStore を内部で解決）
    private val vm: NotesViewModel by viewModels { NotesViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                // ★ keep: 明示的に VM を渡す（AppScaffold 側で viewModel() は呼ばない）
                AppScaffold(vm = vm)
            }
        }
    }
}
