// ルートは“宣言だけ”。ここに android{} や dependencies{} は書かない！
plugins {
    alias(libs.plugins.android.application) apply false   // ルートでは apply false
    alias(libs.plugins.kotlin.android)      apply false   // ルートでは apply false
    alias(libs.plugins.kotlin.compose)      apply false   // 使うなら宣言のみ
    alias(libs.plugins.ksp)                 apply false   // ★ 追加：app 側で使うための宣言だけ
}
