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

// keep: ルート定義
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
    modifier: Modifier = Modifier,
    navController: NavHostController,
    vm: NotesViewModel,
) {
    NavHost(
        navController = navController,
        // ★ Changed: スタート画面を「すべてのノート」に変更
        // アプリ起動時にコックピットのログ一覧が表示される体験を作る
        startDestination = Routes.ALL_NOTES,
        modifier = modifier,
    ) {
        // 一覧（Bugs）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenSearch = { navController.navigateTopLevel(Routes.SEARCH) },
                onOpenFolders = { navController.navigateTopLevel(Routes.FOLDERS) },
                onOpenMindMap = { navController.navigate(Routes.MINDMAP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }
        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }
        // エディタ
        composable(Routes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                onBack = { navController.navigateUp() },
            )
        }
        // MindMap
        composable(Routes.MINDMAP) {
            val mindVm: MindMapViewModel = viewModel()
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
            )
        }
        // 設定
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
            )
        }
        // ★ ALL_NOTES (Home)
        composable(Routes.ALL_NOTES) {
            AllNotesScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
            )
        }
    }
}

// keep: トップレベル遷移ヘルパ
private fun NavHostController.navigateTopLevel(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(this@navigateTopLevel.graph.findStartDestination().id) {
            saveState = true
        }
    }
}
