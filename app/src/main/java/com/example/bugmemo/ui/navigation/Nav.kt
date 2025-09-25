// app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.components.EmptyState
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.MindMapScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen

// 画面ルート定義
sealed class Dest(val route: String, val label: String) {
    data object Bugs : Dest("bugs", "Bugs")
    data object Folders : Dest("folders", "Folders")
    data object MindMap : Dest("mindmap", "MindMap")
    data object Search : Dest("search", "Search")
    data object Editor : Dest("editor/{id}", "Editor") {
        fun route(id: Long) = "editor/$id"
    }
}

// ナビゲーション補助
object NavRoutes {
    fun navigateSingleTop(nav: NavHostController, route: String) {
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

// アプリの NavHost
@Composable
fun AppNavHost(
    nav: NavHostController,
    vm: NotesViewModel,
    modifier: Modifier = Modifier               // ★ Changed: 以前の `padding: Modifier` を `modifier` に改名
) {
    NavHost(
        navController = nav,
        startDestination = Dest.Bugs.route,
        modifier = modifier                     // ★ Changed: ここで受け取った `modifier` をそのまま適用
    ) {
        composable(Dest.Bugs.route) {
            BugsScreen(
                notes = vm.notesSorted,
                onCreate = { vm.createNote() },
                onOpen = { id -> nav.navigate(Dest.Editor.route(id)) }
            )
        }
        composable(Dest.Folders.route) {
            FoldersScreen(
                folders = vm.folders,
                onAddFolder = vm::addFolder
            )
        }
        composable(Dest.MindMap.route) {
            MindMapScreen(notes = vm.notes)
        }
        composable(Dest.Search.route) {
            SearchScreen(
                onQuery = vm::search,
                results = vm.searchResults,
                onOpen = { id -> nav.navigate(Dest.Editor.route(id)) }
            )
        }
        composable(Dest.Editor.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            val note = id?.let(vm::findById)
            if (note != null) {
                NoteEditorScreen(
                    note = note,
                    folders = vm.folders,
                    onSave = { title, content, folder ->
                        vm.updateNote(note.id, title, content, folder)
                        nav.popBackStack()
                    },
                    onDelete = {
                        vm.deleteNote(note.id)
                        nav.popBackStack()
                    }
                )
            } else {
                EmptyState("Note not found")
            }
        }
    }
}
