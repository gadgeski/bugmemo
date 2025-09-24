// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/MainActivity.kt
// ----------------------------------------
package com.example.bugmemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bugmemo.ui.AppScaffold
import com.example.bugmemo.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppScaffold()
            }
        }
    }
}
