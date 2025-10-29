// app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.mindmap.MindMapViewModel
import com.example.bugmemo.ui.screens.AllNotesScreen
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.MindMapScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen
import com.example.bugmemo.ui.screens.SettingsScreen

// ★ keep: トップレベル遷移ヘルパで使用(androidx.navigation.NavGraph.Companion.findStartDestination)
// ★ keep: ルート定義（一本化）
object Routes {
    const val BUGS = "bugs"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val EDITOR = "editor"
    const val MINDMAP = "mindmap"
    const val SETTINGS = "settings"
    const val ALL_NOTES = "all_notes"
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
        // 一覧（Bugs）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                // ★ keep: 一覧からエディタへ（トップレベルではないので通常 push）
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                // ★ Changed: AppBar のショートカットもトップレベル遷移ポリシーで統一
                onOpenSearch = { navController.navigateTopLevel(Routes.SEARCH) },
                onOpenFolders = { navController.navigateTopLevel(Routes.FOLDERS) },
                // ★ keep: MindMap は“隠しルート”なので通常遷移
                onOpenMindMap = { navController.navigate(Routes.MINDMAP) },
                // ★ keep: 設定はトップレベル外（通常遷移）
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                // ★ keep: 検索結果からエディタへ
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                // ★ keep（リネーム後）: Notes（= AllNotes）ショートカットの遷移先を配線（トップレベル遷移）
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }

        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                // ★ keep: フォルダから新規ノート→エディタへ
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                // ★ keep（リネーム後）: Notes（= AllNotes）ショートカットの遷移先を配線（トップレベル遷移）
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }

        // エディタ（トップレベル外）
        composable(Routes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                // ★ keep: 戻る
                onBack = { navController.navigateUp() },
            )
        }

        // MindMap（UI からはリンクを出さない“非表示ルート”）
        composable(Routes.MINDMAP) {
            val mindVm: MindMapViewModel = viewModel()
            // ★ keep: 画面ローカル VM
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
            )
        }

        // 設定（トップレベル外）
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                // ★ keep: 戻るで前画面へ
            )
        }

        // ★ Changed: ALL_NOTES への onBack 配線（navigateUp のフォールバック付き）
        composable(Routes.ALL_NOTES) {
            AllNotesScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onBack = {
                    // ★ Added: 戻り先が無い場合（navigateUp が false）に Bugs へフォールバック
                    if (!navController.navigateUp()) {
                        navController.navigateTopLevel(Routes.BUGS)
                    }
                },
            )
        }
    }
}

/* ===============================
   ★ keep: トップレベル遷移ヘルパ
   - Bugs / Search / Folders / AllNotes の相互遷移で重複スタックを作らない
   - スクロール位置などを save/restore
   =============================== */
private fun NavHostController.navigateTopLevel(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        // ★ keep: 二重積み防止
        restoreState = true
        // ★ keep: 以前の状態（スクロール等）を復元
        popUpTo(this@navigateTopLevel.graph.findStartDestination().id) {
            saveState = true
            // ★ keep: バックスタック先頭で状態保存
        }
    }
}
