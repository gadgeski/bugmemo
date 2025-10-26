// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/theme/AppTheme.kt
// ----------------------------------------
package com.example.bugmemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/* ===============================
   カスタムカラーパレット（黄×橙を基調としたトーン）
   =============================== */

// ★ Changed: 近似のブランド系カラーを定義
private val BrandOrange = Color(0xFFFF8A00)

// 濃いオレンジ（ロゴ円のイメージ）
private val BrandOrangeDark = Color(0xFFF36C00)

// プライマリ容器などの濃色
private val BrandAmber = Color(0xFFFFC400)

// アクセント寄りのオレンジ/黄色
private val BrandBgYellow = Color(0xFFFFF0B3)

// 明るい黄（背景）
private val BrandSurfaceYellow = Color(0xFFFFF6CC)

// もう少し淡い黄（サーフェス）
private val OnDarkInk = Color(0xFF1F1400)
// 暖色系UIに合う濃いインク色（茶寄りの黒）

/* ===============================
   ライトテーマ
   =============================== */
private val LightColors = lightColorScheme(
    // ★ Changed: オレンジ基調
    primary = BrandOrange,
    onPrimary = Color.Black,
    // ★ Changed: 黒インクでコントラストを確保
    primaryContainer = BrandOrangeDark,
    onPrimaryContainer = Color(0xFFFFF4E6),

    // ★ Changed: セカンダリも暖色系で統一
    secondary = BrandAmber,
    onSecondary = Color(0xFF1E1200),
    secondaryContainer = Color(0xFFFFE38A),
    onSecondaryContainer = Color(0xFF231600),

    // ★ Changed: 第三色は少し茶色寄り
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDC9E),
    onTertiaryContainer = Color(0xFF2C1B00),

    // エラーはデフォルト寄り（可読性重視）
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // ★ Changed: 背景/サーフェスは黄色ベース
    background = BrandBgYellow,
    onBackground = OnDarkInk,
    surface = BrandSurfaceYellow,
    onSurface = OnDarkInk,

    // ★ Changed: バリアント/アウトラインも暖色寄りに
    surfaceVariant = Color(0xFFFFE6B8),
    onSurfaceVariant = Color(0xFF5D3A00),
    outline = Color(0xFF9A6B1A),
    outlineVariant = Color(0xFFFFE0A3),

    scrim = Color.Black,
    inverseSurface = Color(0xFF2D2300),
    inverseOnSurface = Color(0xFFFFF3CF),
    inversePrimary = Color(0xFFFFB766),
)

/* ===============================
   ダークテーマ
   =============================== */
private val DarkColors = darkColorScheme(
    // ★ Changed: ダーク側は“琥珀×焦げ茶”
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF3A1A00),
    primaryContainer = Color(0xFF5A2700),
    onPrimaryContainer = Color(0xFFFFE6C8),

    secondary = Color(0xFFFFCC80),
    onSecondary = Color(0xFF3A1A00),
    secondaryContainer = Color(0xFF5A3A00),
    onSecondaryContainer = Color(0xFFFFF0C9),

    tertiary = Color(0xFFE6C389),
    onTertiary = Color(0xFF2C1B00),
    tertiaryContainer = Color(0xFF4B3200),
    onTertiaryContainer = Color(0xFFFFE2B9),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // ★ Changed: 背景/サーフェスは暗い焦げ茶、文字は明るい琥珀
    background = Color(0xFF1B0E00),
    onBackground = Color(0xFFFFE9B0),
    surface = Color(0xFF1B0E00),
    onSurface = Color(0xFFFFE9B0),

    surfaceVariant = Color(0xFF4B3200),
    onSurfaceVariant = Color(0xFFE6C389),
    outline = Color(0xFFB4893A),
    outlineVariant = Color(0xFF4B3200),

    scrim = Color.Black,
    inverseSurface = Color(0xFFFFE9B0),
    inverseOnSurface = Color(0xFF2D2300),
    inversePrimary = BrandOrange,
)

/* ===============================
   テーマ適用
   - customize: (ColorScheme) -> ColorScheme で任意の配色を一括更新
   - 動的カラーを使う/使わないを選択可能
   =============================== */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    primary: Color? = null,
    secondary: Color? = null,
    customize: (ColorScheme) -> ColorScheme = { it },
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val base =
        if (useDynamicColor) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColors else LightColors
        }

    // ★ keep: 必要なら個別に上書き
    val withBasic = base.copy(
        primary = primary ?: base.primary,
        secondary = secondary ?: base.secondary,
    )

    val colorScheme = customize(withBasic)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
