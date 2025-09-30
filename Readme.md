# BugMemo (Android / Kotlin / Compose)

モバイル開発中の**バグやメモを素早く記録**するシンプルなノートアプリ。  
フォルダ分け・検索・マインドマップ表示に対応。

## 主な機能

- 📝 ノート（バグ）作成・編集・削除
- 🗂 フォルダ分け
- 🔎 検索
- 🧭 ボトムナビ（Bugs / Folders / MindMap / Search）
- 🎨 Material3 / カラーテーマ（Compose）

## 技術スタック

- Kotlin, Jetpack Compose (Material3)
- Navigation-Compose, ViewModel
- （データ層）Repository（将来 Room/Datastore に拡張）
- Gradle Version Catalog（`gradle/libs.versions.toml`）

## 動作環境

- **Android Studio** 最新安定版
- **JDK 17**（Temurin 17 など）
- SDK Platforms: `compileSdk = 36` を導入

## 今後の予定

今後永続化は Room / DataStore を予定：

- Room: **スキーマバージョン管理 & Migration 実装**
- DataStore: **キー変更時の移行**（旧キー読取 → 新キー保存）
