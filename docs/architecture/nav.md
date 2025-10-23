# Nav アーキテクチャ概要

## 目的
- 画面遷移を1か所に集約し、拡張（画面追加・遷移方針変更）を安全にする。
- 画面内ショートカット（検索/フォルダなど）とボトムナビの遷移ポリシー統一。

## 管理ファイル
- app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
- object Routes … ルート定義（BUGS, SEARCH, FOLDERS, EDITOR, MINDMAP, SETTINGS）
- @Composable fun AppNavHost(...) … NavHost + 各 composable 登録

## グラフ構成（startDestination と各 Destination）
- startDestination = Routes.BUGS
- Routes.BUGS → BugsScreen
- 引数なし。ハンドラで onOpenEditor などのラムダを受けて遷移。
- Routes.SEARCH → SearchScreen
- Routes.FOLDERS → FoldersScreen
- Routes.EDITOR → NoteEditorScreen
- Routes.MINDMAP → MindMapScreen（画面ローカルな MindMapViewModel = viewModel()）
- Routes.SETTINGS → SettingsScreen（歯車アイコンから遷移）

## 方針
- VM の生成は上位（MainActivity / AppScaffold）で。
- NotesViewModel は Bugs/Search/Folders/Editor で共有。MindMap は独立 VM。

## 遷移ポリシー（重複スタックを作らない）
## 画面内ショートカット
- ボトムナビ共通で以下を徹底：
- launchSingleTop = true（同一画面を二重に積まない）
- restoreState = true（戻った時にスクロール等を復元）
- popUpTo(findStartDestination().id) { saveState = true }（トップレベルは1スタック）
- BugsScreen の AppBar の「検索」「フォルダ」など全てこのポリシーで navigate()。

## 依存関係／import の注意
- androidx.navigation.compose.NavHost, composable
- findStartDestination(), hierarchy（選択状態の判定に使用）
- VM は外から渡す（NotesViewModel）。MindMapViewModel のみ viewModel() でローカル生成。

## 画面追加の手順（チェックリスト）
- Routes に定数を追加（例：const val SETTINGS = "settings"）。
- AppNavHost に composable(Routes.SETTINGS) { SettingsScreen(...) } を追加。
- 呼び出し元（例：BugsScreen のアイコン）から遷移ラムダを配線。
- トップレベル化するならBottomNav への追加＋選択状態の判定更新。
- 文字列（CD 含む）を strings.xml に追加。

## よくある落とし穴
## 別画面から戻れない
- ボトムナビ・ショートカット双方で前述の navigate オプション統一が必須。
## VM が二重生成
- viewModel() を画面内で呼ばず、MainActivity/AppScaffold から渡す（MindMap だけ例外）。
## CD の付け忘れ
- アイコン contentDescription は strings に集約。
