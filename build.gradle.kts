// ルートは“宣言だけ”。ここに android{} や dependencies{} は書かない！
plugins {
    alias(libs.plugins.android.application) apply false   // ★ ルートでは apply false
    alias(libs.plugins.kotlin.android) apply false        // ★ ルートでは apply false
    alias(libs.plugins.kotlin.compose) apply false     // 使うならここで宣言のみ
}
