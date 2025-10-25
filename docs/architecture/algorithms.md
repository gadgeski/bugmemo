# Algorithms & Behaviors

## 1) Navigation Policy（トップレベル3画面の遷移）
- Goal: 重複スタックを作らず、タブ間移動で状態を保存/復元する。
- Approach: `findStartDestination` へ `popUpTo(saveState=true)`、`launchSingleTop=true`、`restoreState=true`。

## Pseudo:
    ```kotlin
    navController.navigate(route) {
      launchSingleTop = true
      restoreState = true
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
    }
    ```
- Notes: 上部アイコンからの遷移も同ヘルパを使用（挙動統一）。

## 2) MindMap 表示（フェーズ1：リスト＋枝線）
- Goal: ツリーをフラット化してインデント表示、枝線で親子関係を視覚化。
- Approach: ViewModel で DFS して (node, depth) のリストを生成。各行は drawBehind で左ガターに縦線＋短い横線（L字）を描画。
- Complexity: フラット化 O(N)、描画は行単位（LazyColumn）。

## Pseudo:
    ```kotlin
    if (depth > 0) drawBehind {
      val y = size.height / 2f
      drawLine(color, Offset(0f,0f), Offset(0f,size.height), stroke) // vertical
      drawLine(color, Offset(0f, y), Offset(12.dp.toPx(), y), stroke) // horizontal arm
    }
    ```
## Drawing (row):
    ```kotlin
    if (depth > 0) drawBehind {
      val y = size.height / 2f
      drawLine(color, Offset(0f,0f), Offset(0f,size.height), stroke) // vertical
      drawLine(color, Offset(0f, y), Offset(12.dp.toPx(), y), stroke) // horizontal arm
    }
    ```
- Undo: 削除時は Snackbar のアクションで undoDelete()（LIFO スタック）。

## 3) Search & Folder Filter
- Goal: 文字入力で即時検索し、必要に応じてフォルダで絞り込み。
- Approach: vm.query と vm.filterFolderId を組み合わせ、クエリ空なら通常一覧、非空なら検索結果ストリームを使う。
- Complexity: シンプルな部分一致なら O(N) / 入力イベント。

## 4) Markdown Bold トグル（最小）
- Goal: 選択範囲を **...** で囲む。未選択なら **** を挿入しキャレットを内側へ。
- Pseudo:
    ```kotlin
    if (selection.isCollapsed) {
      insert("****"); moveCursor(-2)
    } else {
      wrap("**", "**")
    }
    ```
- Notes: エスケープやネストは今後の拡張で対応。
## 5) Editor Font Scale（ユーザ設定）
- Goal: 本文/タイトルのフォントをユーザ設定で拡大縮小。
- Approach: DataStore に editor_font_scale（Float, 0.5–2.0）を保存し、collectAsState で購読。Text/TextField にスケールを掛ける。
- Pseudo:
    ```kotlin
    val scale by AppLocaleManager.editorFontScaleFlow(ctx).collectAsState(1.0f)
    val titleStyle = MaterialTheme.typography.titleLarge.copy(fontSize = base * scale)
    ```
Notes: rememberSaveable により Settings 画面で編集中も回転で保持（任意）。
## 6) i18n（言語切替）

- Goal: アプリ内から ja/en/システム を切替、再起動なしで反映。
- Approach: DataStore(language_tag) に保存し、AppCompatDelegate.setApplicationLocales() で即時反映。空文字はシステム追従。
- Pseudo:
    ```kotlin
    val locales = if (tag.isBlank()) LocaleListCompat.getEmptyLocaleList()
              else LocaleListCompat.forLanguageTags(tag)
    AppCompatDelegate.setApplicationLocales(locales)
    ```
## 7) Accessibility / Usability 小ネタ
- 枝線色は onSurfaceVariant を使用（ダーク/ライトで視認性確保）。
- BottomNav は Editor/Settings/MindMap では非表示（集中できるように）。
