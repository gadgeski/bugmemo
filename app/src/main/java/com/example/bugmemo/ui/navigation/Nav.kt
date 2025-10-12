// app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.mindmap.MindMapViewModel
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.MindMapScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen

// ★ Added: MindMap 用の viewModel() を使うための関数を import（androidx.lifecycle.viewmodel.compose.viewModel）
// ★ Added: MindMap 画面の Composable を import（MindMapScreen）
// ★ Added: MindMap 画面用の ViewModel を import（MindMapViewModel）

object Routes {
    const val BUGS = "bugs"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val EDITOR = "editor"
    const val MINDMAP = "mindmap"
    // ★ Added: Mind Map のルート定義（UI からは出さない想定）
}

@Composable
fun AppNavHost(
    // ★ keep: デフォルトは持たず AppScaffold から受け取る
    modifier: Modifier = Modifier,
    navController: NavHostController,
    vm: NotesViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BUGS,
        modifier = modifier,
    ) {
        // 一覧（バグメモ）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                // ★ keep: 一覧からエディタへ
                onOpenEditor = {
                    navController.navigate(Routes.EDITOR)
                },
            )
        }

        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                // ★ keep: 検索結果からエディタへ
                onOpenEditor = {
                    navController.navigate(Routes.EDITOR)
                },
            )
        }

        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                // ★ keep: フォルダ画面からも遷移可能
                onOpenEditor = {
                    navController.navigate(Routes.EDITOR)
                },
            )
        }

        // エディタ
        composable(Routes.EDITOR) {
            // ★ keep: 戻る
            NoteEditorScreen(
                vm = vm,
                onBack = { navController.navigateUp() },
            )
        }

        // ★ Added: MindMap（UI からはリンクを出さない“非表示ルート”）
        // ★ Added: 既存の Notes 用 VM とは独立した InMemory の VM を画面ローカルに生成
        composable(Routes.MINDMAP) {
            // ★ Added: MindMap 用の VM を取得（画面ローカル）
            val mindVm: MindMapViewModel = viewModel()
            // ★ Added: 画面を閉じるだけ（戻る）
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
            )
        }
    }
}
