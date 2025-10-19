// app/build.gradle.kts — フル版（Lint最小 + Roomスキーマ出力/読込 + BuildConfig 生成を明示）
plugins {
    alias(libs.plugins.android.application) // OK
    alias(libs.plugins.kotlin.android) // OK
    alias(libs.plugins.kotlin.compose) // OK（Compose Compiler DSL）
    alias(libs.plugins.ksp) // OK（Room の KSP）
    // ★ 重複なし：先頭で二重に同じ alias を適用しないこと
}

android {
    namespace = "com.example.bugmemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bugmemo"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // ★ Added: Release では MindMap をデフォルト OFF
            buildConfigField("boolean", "ENABLE_MIND_MAP", "false")
        }
        debug {
            // ★ Added: Debug では MindMap を ON
            buildConfigField("boolean", "ENABLE_MIND_MAP", "true")
        }
        // ★ Note: 他の buildType を追加する場合は同様に buildConfigField を定義
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        // ★ Added: BuildConfig を明示的に生成（BuildConfig.* を安全に使うため）
        buildConfig = true
    }

    // ───────────── Lint 最小設定（CI/ローカル共通）─────────────
    lint {
        abortOnError = true // ★ 重大な指摘でビルド失敗（推奨）
        warningsAsErrors = false // ★ 警告はまず許容。整備後 true に上げるのがおすすめ
        baseline = file("lint-baseline.xml") // ★ 既存指摘を固定化（ファイルが無ければ無視）
    }

    // ★ Added: Room のスキーマを androidTest の assets に含める
    //   （exportSchema = true を活かして、マイグレーションの回帰テストがしやすくなります）
    sourceSets["androidTest"].assets.srcDirs(files("$projectDir/schemas"))
}

// ★ Added: KSP に Room スキーマ出力先を指示（exportSchema = true とセットで）
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // Compose（BOM 配下）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ★ 注意：Version Catalog に "androidx-compose-material-icons-extended" がある前提
    implementation(libs.androidx.compose.material.icons.extended)

    // Activity / Lifecycle / Navigation（BOM外のため catalog で版管理）
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // DataStore（フォルダ絞り込み・検索語の保存/復元で使用）
    implementation(libs.androidx.datastore.preferences)

    // Appcompat
    implementation(libs.androidx.appcompat)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // ★ KSP 構成で呼ぶ。版は libs.versions.toml の room に従う

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // （必要時のみ残す）
    // implementation(libs.androidx.lifecycle.runtime.ktx)
}
