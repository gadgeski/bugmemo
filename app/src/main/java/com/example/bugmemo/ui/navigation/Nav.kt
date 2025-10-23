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
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.MindMapScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen
import com.example.bugmemo.ui.screens.SettingsScreen

// ★ Added: startDestination を取得して popUpTo に使うための拡張を import(androidx.navigation.NavGraph.Companion.findStartDestination)
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
    // ★ keep: 設定画面のルート
}

@Composable
fun AppNavHost(
    // ★ keep: デフォルトは持たず AppScaffold から受け取る
    modifier: Modifier = Modifier,
    navController: NavHostController,
    vm: NotesViewModel,
) {
    // ─────────────────────────────────────────────────────────────
    // ★ Added: トップレベル画面（Bugs/Search/Folders）へ遷移するための“共通ラムダ”
    //          - launchSingleTop: 二重積み上げ防止
    //          - restoreState   : 以前の状態（スクロール位置など）復元
    //          - popUpTo(start) : トップレベル間の重複スタック化を防止（saveStateで戻れる）
    val navigateTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
    // ─────────────────────────────────────────────────────────────

    NavHost(
        navController = navController,
        startDestination = Routes.BUGS,
        modifier = modifier,
    ) {
        // 一覧（バグメモ）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                // ★ keep: 一覧からエディタへ（詳細画面なので通常 navigate）
                onOpenEditor = { navController.navigate(Routes.EDITOR) },

                // ★ Changed: トップレベルへの遷移は共通ラムダ経由に統一
                onOpenSettings = { navigateTopLevel(Routes.SETTINGS) }, // ★ Changed
                onOpenFolders = { navigateTopLevel(Routes.FOLDERS) }, // ★ Changed
                onOpenSearch = { navigateTopLevel(Routes.SEARCH) }, // ★ Changed

                // ★ keep: MindMap は“非トップ”の隠し画面想定なので通常 navigate
                onOpenMindMap = { navController.navigate(Routes.MINDMAP) },
            )
        }

        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                // ★ keep: 検索結果からエディタへ（通常 navigate）
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
            )
        }

        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                // ★ keep: フォルダ画面からも遷移可能（通常 navigate）
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
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

        // 設定
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                // ★ 戻るで前画面へ
            )
        }
    }
}
