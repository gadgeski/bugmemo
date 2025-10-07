// <project-root>/build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)      apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.ksp)                 apply false

    alias(libs.plugins.spotless) // Spotless をルートで適用
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**", "**/generated/**")

        // ★ Fixed: editorConfigOverride を ktlint() にチェーン
        ktlint() // 例: ktlint("1.3.1")
            .editorConfigOverride(
                mapOf(
                    // @Composable / @Preview の PascalCase を許容
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview"
                )
            )

        // 例：他のルールを無効化したくなったら下記を追加
        // .editorConfigOverride(mapOf("ktlint_standard_filename" to "disabled"))
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")

        // ★ Fixed: こちらもチェーンで指定
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview"
                )
            )
    }

    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore")
        targetExclude("**/build/**", ".gradle/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
