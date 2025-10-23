// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

// ★ Added: トップレベル遷移のために startDestination を取得する拡張を import(androidx.navigation.NavGraph.Companion.findStartDestination)
// ★ Changed: 表示/非表示判定でも使うために hierarchy を活用(androidx.navigation.NavDestination.Companion.hierarchy)

@Composable
fun AppScaffold(
    // ★ keep: 呼び出し側で生成した VM を受け取る
    vm: NotesViewModel,
) {
    val navController = rememberNavController()

    // ★ Changed: currentRoute 文字列比較 → Destination + hierarchy 判定に切替
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { e ->
            when (e) {
                is NotesViewModel.UiEvent.Message -> {
                    snackbarHostState.showSnackbar(
                        message = e.text,
                        withDismissAction = true,
                    )
                }
                is NotesViewModel.UiEvent.UndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "削除しました",
                        actionLabel = "取り消す",
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.undoDelete()
                    }
                }
            }
        }
    }

    // ★ keep: BottomNav に載せるトップレベル3画面
    val navItems = listOf(
        NavItem("Bugs", Routes.BUGS) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs")
        },
        NavItem("Search", Routes.SEARCH) {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        NavItem("Folders", Routes.FOLDERS) {
            Icon(Icons.Filled.Folder, contentDescription = "Folders")
        },
    )

    // ★ Added: BottomNav 表示対象のルート集合（Routes に同様の set があるならそれを参照してもOK）
    val bottomBarRoutes = setOf(Routes.BUGS, Routes.SEARCH, Routes.FOLDERS)

    // ★ Added: 現在の Destination が BottomNav 対象かどうかを階層で判定
    val showBottomBar = shouldShowBottomBar(currentDestination, bottomBarRoutes)

    // ─────────────────────────────────────────────────────────────
    // ★ Added: トップレベル画面間の“共通ナビゲーション”ヘルパ
    //   - launchSingleTop: 二重積み上げ防止
    //   - restoreState   : 以前の状態（スクロール等）復元
    //   - popUpTo(start) : トップレベル間の重複スタック化を防止（saveState で戻れる）
    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
    // ─────────────────────────────────────────────────────────────

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // ★ Added: 表示/非表示をここで切り替え（EDITOR/SETTINGS/MINDMAP では非表示）
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        // ★ Changed: 階層内に一致する route があるかで選択状態を判定（ネストに強い）
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                // ★ Changed: 直接 navigate を書かず共通ヘルパを使用
                                if (!selected) navigateTopLevel(item.route)
                            },
                            icon = { item.icon() },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
        // ★ Added: bottomBar 引数の後ろにトレーリングカンマを追加（マルチライン呼び出し推奨）
    ) { innerPadding ->
        // ★ keep: imePadding でキーボード重なりを回避
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier
                .padding(innerPadding)
                .imePadding(),
        )
    }
}

/** ボトムバー用モデル */
private data class NavItem(
    val label: String,
    val route: String,
    val icon: @Composable () -> Unit,
)

// ★ Added: BottomNav 表示判定ヘルパー（階層を見て安全に判定）
private fun shouldShowBottomBar(
    destination: NavDestination?,
    bottomBarRoutes: Set<String>,
): Boolean {
    // destination が null（初期描画中など）の時は表示しても OK にしておく（必要なら false にしても良い）
    if (destination == null) return true
    // ★ Added: 初期状態の見た目優先（任意で false に変更可）
    return destination.hierarchy.any { it.route in bottomBarRoutes }
}
