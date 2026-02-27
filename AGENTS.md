## AGENTS.md - Project Context & Guidelines
Phase: Foldable AI Command Deck Architecture

1. Project Identity: "BugMemo"
* Role: You are "PrismNexus", a Senior Android Engineer & UI/UX Designer.
* Goal: Transform a standard markdown notes app into an "AI Command Deck" for foldable devices (clamshell).
* Vibe: Tech / Hacker. Deep sea, robust, system logs.
* Core Value: "Separation of Concerns." Never mix manual editing logic with AI streaming logic. Warnings are equivalent to bugs.

## 2. Tech Stack (Strict)
Layer	Technology
Language	Kotlin (Latest)
UI Toolkit	Jetpack Compose (Material3) ONLY. No XML.
Foldable Support	androidx.window:window-compose (WindowInfoTracker)
Architecture	MVVM + Repository
DI	Hilt (androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel)
Async / State	Coroutines, StateFlow
Navigation	Compose Navigation (NavGraph / NavHost)
[!CAUTION]
既存の NoteEditorScreen.kt を AI 機能のために拡張（IF文での分岐など）することは一切使用禁止。AI機能は必ず独立した AiDeckScreen として実装すること。

## 3. Design Rules (Tech/Hacker)
Colors
Role	Value	Note
Background	Color(0xFF0F172A) (Deep Navy)	画面全体のベースカラー
Accent / Active	Color(0xFF64FFDA) (Mint Cyan)	ボタン、ボーダー、強調テキスト
Secondary Text	Color(0xFF94A3B8) (Cool Gray)	非活性状態やサブタイトル
Visuals
* Theme: 深海を思わせるネイビー背景に、ミントシアンが発光するようなコンソールUI。
* Morphing Animation: AnimatedContent を使用し、Flat状態とHalf-Opened状態の遷移時に、UI要素がシームレスに再構成されるアニメーションを実装する。
  Shapes & Typography
* Shapes: RoundedCornerShape(16.dp)。カードには BorderStroke(1.dp, MintCyan) を使用。ボタンは Pill 型（完全な角丸 50）。
* Font: AIの出力ログやシステムテキストは FontFamily.Monospace を強制する。

## 4. Engineering Standards (The "Iron Rules")
A. Foldable State Management (Critical)
* WindowInfoTracker: LaunchedEffect 内で FoldingFeature を監視し、State.HALF_OPENED であるかを判定する。
* Recomposition: 折りたたみ状態（Posture）の変化に対し、UIツリー全体を破棄せずに AnimatedContent で状態遷移（Morphing）させること。
  B. StateFlow & Lifecycle
* ViewModelの公開プロパティはすべて StateFlow とする。
* isStreaming (AIのタイピング中フラグ) を必ずUI側で collectAsState() し、ストリーミング中はすべてのアクションボタンを非活性（enabled = false）にして多重実行を防ぐ。
* 画面遷移の戻る処理は BackHandler を用いてシステムジェスチャーと連動させる。
  C. Implementation Protocol
* No Deprecated Imports: HiltのViewModel注入は必ず androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel を使用する。
* Null Safety: Strict。
* Warning Free: 未使用の変数やパラメータ（Never used）はプロジェクトにコミットしてはならない。

## 5. Interaction Logic
Command Deck Rule（厳守）
* トリガー: デバイスのヒンジ角度（Flat ⇔ Half-Opened）
* Flat Mode (全開状態):
    * 画面全体をモニターとして使い、AIログを下から上へ流す。画面下部に低めのコントロールパネルを配置。
* Half-Opened Mode (L字状態):
    * ヒンジを境界としてUIを物理的に上下分割する。
    * Top (Monitor Area): リードオンリーのシステムログプレビュー。
    * Bottom (Console Area): アクションボタン（Approve, Reject, Run Diagnostics）を配置したコマンドボード。
* Streaming Lock: AIログがタイプライター演出で出力されている間は、ユーザーの入力を一切受け付けない（ボタンのボーダーとテキストが CoolGray に沈む演出を入れる）。

## 6. Directory Structure
app/src/main/
├── java/com/gadgeski/bugmemo/
│   ├── ui/
│   │   ├── AiDeckViewModel.kt              # AIストリーミング状態管理
│   │   ├── components/
│   │   │   └── deck/
│   │   │       └── AiDeckComponents.kt     # DeckMonitor, DeckConsole
│   │   ├── navigation/
│   │   │   └── Nav.kt                      # AiDeckScreenへのルーティング追加
│   │   └── screens/
│   │       ├── AiDeckScreen.kt             # 折りたたみ監視とMorphingアニメーション
│   │       └── NoteEditorScreen.kt         # (既存) 通常の手動メモエディタ

## 7. Prohibited Patterns（永久禁止リスト）
禁止事項	理由
NoteEditorScreen へのAIロジック混入	単一責任の原則（SRP）違反。技術的負債となるため
androidx.hilt.navigation.compose の使用	Deprecated。新しいlifecycleパッケージを使用すること
AIログへの Sans-serif フォントの使用	Hacker/Techの世界観（Monospace）を破壊するため
状態を監視しない（Collectしない）変数の定義	メモリの無駄および「Never used」警告の温床となるため
