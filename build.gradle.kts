// <project-root>/build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false

    // ★ Added: Hilt Plugin の定義 (apply false でルートに登録)
    // アプリ側と同じバージョン "2.51.1" を指定しておきます。
    // バージョンカタログ(libs.plugins.hilt)がある場合は alias(...) に置き換えてください。
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**", "**/generated/**")

        // ★ Fixed: editorConfigOverride を ktlint() にチェーン
        ktlint()
            .editorConfigOverride(
                mapOf(
                    // @Composable / @Preview の PascalCase を許容
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview",
                ),
            )
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")

        // ★ Fixed: こちらもチェーンで指定
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview",
                ),
            )
    }

    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
