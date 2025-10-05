// <project-root>/build.gradle.kts
// ルートは“宣言だけ”が基本ですが、Spotless はルートで「適用して」全体を整形します。

plugins {
    alias(libs.plugins.android.application) apply false   // ルートでは apply false
    alias(libs.plugins.kotlin.android)      apply false   // ルートでは apply false
    alias(libs.plugins.kotlin.compose)      apply false   // 使うなら宣言のみ
    alias(libs.plugins.ksp)                 apply false   // app 側で使うための宣言だけ

    alias(libs.plugins.spotless)                            // ★ Added: ルートで Spotless を適用
}

// ★ Added: Spotless 設定（ルートで全体に効かせる）
spotless {
    // Kotlin ファイル（モジュール横断）
    kotlin {
        // すべてのサブプロジェクト配下の .kt を対象に（ルート適用でも問題なく拾えます）
        target("**/*.kt")
        // 生成物などは除外
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**", "**/generated/**")

        // デフォルトの ktlint を使用（必要になったらバージョン固定も可能）
        ktlint() // 例: ktlint("1.3.1")
        // 追加ルールが必要な場合は適宜 editorConfigOverride を設定
        // editorConfigOverride(mapOf("ktlint_standard_filename" to "disabled"))
    }

    // Gradle Kotlin スクリプト（build.gradle.kts 等）
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        ktlint()
    }

    // 任意：その他のテキスト系の最低限整形（末尾スペース除去・改行）
    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// （任意）マルチモジュールでも確実に拾いたい場合は、必要に応じて subprojects にも適用可能。
// 今回はルートの target("**/*.kt") で全体をカバーしているため不要です。
// subprojects {
//     apply(plugin = "com.diffplug.spotless")
// }
