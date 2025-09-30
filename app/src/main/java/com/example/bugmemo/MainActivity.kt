package com.example.bugmemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels                        // ★ Room DI: Activity KTX（by viewModels）
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bugmemo.ui.AppScaffold
import com.example.bugmemo.ui.theme.AppTheme

// ★ Room DI: import（あなたのパッケージ構成に合わせて調整してください）
import com.example.bugmemo.data.RoomNotesRepository
import com.example.bugmemo.data.NotesRepository
import com.example.bugmemo.data.db.AppDatabase
import com.example.bugmemo.ui.NotesViewModel

class MainActivity : ComponentActivity() {

    // ★ Room DI: Repository を用意して ViewModel に注入する Factory
    private val notesViewModelFactory by lazy {
        val db = AppDatabase.get(applicationContext)              // ★ Room DI: DBシングルトンを取得
        val repo: NotesRepository = RoomNotesRepository(          // ★ Room DI: Repository を Room 実装に
            db.noteDao(),
            db.folderDao()
        )
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                    return NotesViewModel(repo) as T              // ★ Room DI: ViewModel に repo 注入
                }
                throw IllegalArgumentException("Unknown ViewModel: $modelClass")
            }
        }
    }

    // ★ Room DI: Activity スコープの ViewModel を Factory 経由で取得
    private val vm: NotesViewModel by viewModels { notesViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                // ★ Room DI: 明示的に DI 済みの ViewModel を渡す（AppScaffold 側で viewModel() は呼ばない）
                AppScaffold(vm = vm)
            }
        }
    }
}
