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
import com.example.bugmemo.ui.screens.SettingsScreen

// ★ Added: 設定画面を NavGraph に載せるため import
// ★ Added: MindMap 用の viewModel() を使うための関数を import（androidx.lifecycle.viewmodel.compose.viewModel）
// ★ Added: MindMap 画面の Composable を import（MindMapScreen）
// ★ Added: MindMap 画面用の ViewModel を import（MindMapViewModel）

// ★ keep: ルート定義（このファイルに一本化）
object Routes {
    const val BUGS = "bugs"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val EDITOR = "editor"
    const val MINDMAP = "mindmap"
    const val SETTINGS = "settings"
// ★ Added: 設定画面のルート
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
                // ★ Added: 設定へ遷移を配線
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                // ★ Added: フォルダ・検索・MindMap への遷移を NavGraph に配線（デフォルト{}だと遷移しないため）
                onOpenFolders = {
                    navController.navigate(Routes.FOLDERS)
                },
                onOpenSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onOpenMindMap = {
                    navController.navigate(Routes.MINDMAP)
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

        // MindMap（UI からはリンクを出さない“非表示ルート”）
        // ★ keep: 既存の Notes 用 VM とは独立した InMemory の VM を画面ローカルに生成
        composable(Routes.MINDMAP) {
            // ★ keep: MindMap 用の VM を取得（画面ローカル）
            val mindVm: MindMapViewModel = viewModel()
            // ★ keep: 画面を閉じるだけ（戻る）
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
            )
        }

        // ★ Added: 設定
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                // ★ Added: 戻るで前画面へ
            )
        }
    }
}
