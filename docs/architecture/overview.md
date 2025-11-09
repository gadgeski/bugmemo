# BugMemo — Overview

# 特徴
## 堅牢化
- Room v4 Migration（FTS4 *external content* + `content_rowid=id`、補助 Index 同期）で**起動時クラッシュゼロ化**。

## 検索
- FTS + プレフィックス `MATCH "語"*`、入力**デバウンス**で無駄検索抑制。

## FTS4について
- Roomが標準でサポートしているFTS4が簡単で便利なアノテーションが提供されている為、その機能を利用するのが最も手軽。

## 再現性
`exportSchema=true` / スキーマJSONをGit管理、移行経路 v1→v4 を明文化。

---

# 設計メモ
## 単方向データフロー
- ViewModel(StateFlow) → Compose(collectAsState) → UI

## 差分更新
- Paging/Indexで DB 側最適化、UI は必要部だけ再コンポーズ

## 副作用の封じ込め
- LaunchedEffect / remember / debounce + flatMapLatest

# 今回の要点
- `@Database(version = 4)`／`MIGRATION_3_4`：既存 FTS を **DROP→正定義**（`content_rowid=id`）で再作成→`REBUILD`。
- `notes` 補助 Index：`folderId` / `updatedAt` / `isStarred` を **IF NOT EXISTS** で整備。
- `folders(name)` Index も存在保証。
- Compose + Paging3 + DataStore（検索語などの軽量設定保持）。

## 効果（例）
> 実測値は端末差あり。あなたの数値に差し替え。
- 一覧初回ロード（N=100k seed）：**XXXms → YYYms**
- FTS検索（10文字）：**AAAms → BBBms**（デバウンス 300ms 適用）

---
