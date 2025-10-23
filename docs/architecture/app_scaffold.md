# AppScaffold アーキテクチャ概要

本書は `AppScaffold.kt` の役割と実装方針を、面談やレビューで説明しやすい粒度で整理したものです。

## 目的
- アプリ全体の「足場」：`Scaffold`（BottomNav / Snackbar）＋ `NavHost`（画面遷移）＋ 共通 UI イベント購読。

## 構成要素
- **NavController**: `rememberNavController()` で生成。遷移/BackStackを管理。
- **現在地の状態化**: `currentBackStackEntryAsState()` → UI と宛先の同期。
- **Snackbar**: `SnackbarHostState` を共有し、ViewModel の UI イベントを表示。

## UI イベント購読
`NotesViewModel.UiEvent` を購読してスナックバー表示。
- Message: `showSnackbar(message, withDismissAction=true)`
- UndoDelete: “削除しました / 取り消す” を表示 → 押下時に `vm.undoDelete()`

## BottomNav（選択判定と遷移ポリシー）
- **選択状態**: `currentDestination?.hierarchy?.any { it.route == item.route } == true`
  → ネストしたグラフでも正しく選択表示。
- **遷移ポリシー**（重複スタックを作らない & 状態復元）:

```kotlin
navController.navigate(item.route) {
    launchSingleTop = true         // 同一画面の二重積み上げを防止
    restoreState = true            // タブ戻りでスクロール位置などを復元
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true           // 以前の状態を保存
    }
}
