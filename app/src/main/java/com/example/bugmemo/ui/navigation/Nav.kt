// app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen

object Routes {
    const val BUGS = "bugs"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val EDITOR = "editor"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,                 // ★ Changed: デフォルトを削除（AppScaffold から受け取る）
    vm: NotesViewModel                                // ★ Changed: デフォルトを削除（AppScaffold から受け取る）
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BUGS,
        modifier = modifier
    ) {
        // 一覧（バグメモ）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                onOpenEditor = {
                    // ★ keep: 一覧からエディタへ
                    navController.navigate(Routes.EDITOR)
                }
            )
        }

        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                onOpenEditor = {
                    // ★ keep: 検索結果からエディタへ
                    navController.navigate(Routes.EDITOR)
                }
            )
        }

        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                onOpenEditor = {
                    // ★ keep: フォルダ画面からも遷移可能
                    navController.navigate(Routes.EDITOR)
                }
            )
        }

        // エディタ
        composable(Routes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                onBack = { navController.navigateUp() } // ★ keep: 戻る
            )
        }
    }
}
