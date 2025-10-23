# SettingsScreen + AppLocaleManager アーキテクチャ概要

## 目的
    - 設定画面からアプリ内で言語とエディタ文字サイズを切り替え、即時反映できるようにする。
## 管理ファイル
    - 画面：app/src/main/java/com/example/bugmemo/ui/screens/SettingsScreen.kt
    - 永続化：app/src/main/java/com/example/bugmemo/core/AppLocaleManager.kt
    - 文字列：app/src/main/res/values/strings.xml（ja / en あり）

## データモデル（DataStore）
    - PREFS_NAME = "app_settings"

## キー
    - language_tag: String（""=システム追従 / "ja" / "en"）
    - editor_font_scale: Float（例：0.85f..1.40f の範囲）

## API
    - languageTagFlow(context): Flow<String>
    - setLanguage(context, tag: String) → DataStore 保存＋AppCompatDelegate.setApplicationLocales(...)
    - editorFontScaleFlow(context): Flow<Float>
    - setEditorFontScale(context, scale: Float)

## SettingsScreen 構成
    - TopAppBar：title_settings
## 言語
    - 表示：pref_language タイトル＋ラジオ 3択
    - 状態：languageTagFlow(ctx) を購読 → selected（remember(languageTag) 初期化）
    - ボタン：
        - action_close：戻る
        - action_apply：setLanguage() → 必要なら activity?.recreate()（即時反映）

## エディタ文字サイズ
    - 表示：pref_editor_font_scale 見出し、pref_editor_font_scale_value（現在値）
    - スライダ：0.85f..1.40f、steps = 0（連続）
    - 状態：editorFontScaleFlow(ctx) を購読 → scaleUi（rememberSaveable で回転/再生成も保持可）
    - 反映：onValueChangeFinished で setEditorFontScale(ctx, scaleUi) を保存即時プレビューをしたい場合は onValueChange で UI 側にも反映（NoteEditor でスケール適用済み）

## NoteEditorScreen での適用（参照）
    - タイトル・本文の Text / OutlinedTextField に fontSize = base * scale を適用済み。
    - これにより Settings のスライダ変更が視認できる（保存後 or 即時）。

## 依存関係
    - androidx.appcompat:appcompat（AppCompatDelegate 用）
    - DataStore: androidx.datastore:datastore-preferences
    - Compose: Material3 / Runtime / Lifecycle（collectAsStateWithLifecycle）

## i18n / strings（代表）
    - title_settings, pref_language, pref_language_system, pref_language_ja, pref_language_en
    - action_apply, action_close
    - pref_editor_font_scale, pref_editor_font_scale_value（例："Editor text size: %.0f%%"）

## 用語解説
i18n = “internationalization（インターナショナリゼーション）
    - ソフトウェアを 多言語・多地域で使えるように設計／実装すること。

具体例（Android 文脈）：
    - 文字列を strings.xml に集約し、values-ja/values-en などで翻訳を切り替え

## UX メモ
    - 言語適用は即時だが、Compose ツリー再生成のため activity?.recreate() を併用が無難。
    - フォントスケールは即時プレビュー or 適用時反映の二段構え。現実装は「スライダ離したら保存」で十分に軽量。

## よくある落とし穴
    - “関数未使用” 警告：setEditorFontScale を呼ぶまで出る。Settings のスライダ保存で解消。
    - 言語タグが空："" は「システム追従」。LocaleListCompat.getEmptyLocaleList() を適用。
    - 範囲外のスケール：スライダ値は coerceIn(0.85f, 1.40f) でガード。
    - CD/文言のハードコード：Settings のタイトルやボタン含め、strings に集約。
