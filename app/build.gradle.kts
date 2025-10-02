plugins {
    alias(libs.plugins.android.application)      // OK
    alias(libs.plugins.kotlin.android)           // OK
    alias(libs.plugins.kotlin.compose)           // OK（Compose Compiler DSL）
    alias(libs.plugins.ksp)                      // OK（Room の KSP） // ★ Fixed: 重複を削除
    // ★ Fixed: 下の2行は重複だったので削除
    // alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android)
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
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17   // そのままOK
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)                               // そのままOK
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)           // setContent{}
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ★ 注意: Version Catalog に "androidx-compose-material-icons-extended" が無いと赤くなります
    implementation(libs.androidx.compose.material.icons.extended)

    // Compose連携
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // （必要なときだけ残す）
    // implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences) // ★ Added: Preferences DataStore
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)  // ← ここは “ksp 構成”で呼ぶだけ。版は TOML の room に従う
}