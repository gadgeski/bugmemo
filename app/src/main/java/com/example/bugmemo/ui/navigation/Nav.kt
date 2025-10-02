package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.screens.*

object NavRoutes {
    const val BUGS = "bugs"
    const val EDITOR = "editor"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val MINDMAP = "mindmap"                 // ★ Added
}

@Composable
fun AppNavHost(
    nav: NavHostController,
    vm: NotesViewModel,
    startDestination: String = NavRoutes.BUGS
) {
    NavHost(navController = nav, startDestination = startDestination) {
        composable(NavRoutes.BUGS) {
            BugsScreen(
                vm = vm,
                onOpenEditor = { nav.navigate(NavRoutes.EDITOR) },
                onOpenSearch = { nav.navigate(NavRoutes.SEARCH) }
            )
        }
        composable(NavRoutes.EDITOR) { NoteEditorScreen(vm = vm, onClose = { nav.popBackStack() }) }
        composable(NavRoutes.SEARCH) { SearchScreen(vm = vm, onClose = { nav.popBackStack() }) }
        composable(NavRoutes.FOLDERS) { FoldersScreen(vm = vm, onClose = { nav.popBackStack() }) }
        composable(NavRoutes.MINDMAP) {                // ★ Added
            MindMapScreen(onClose = { nav.popBackStack() })
        }
    }
}
