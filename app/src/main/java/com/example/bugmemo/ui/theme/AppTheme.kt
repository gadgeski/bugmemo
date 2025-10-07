// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/theme/AppTheme.kt
// ----------------------------------------
package com.example.bugmemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme     // ★ Added: 動的カラー（ダーク）
import androidx.compose.material3.dynamicLightColorScheme    // ★ Added: 動的カラー（ライト）
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext              // ★ Added: dynamic color に必要
// import android.os.Build                                   // ★ Removed: minSdk=34 なので分岐を削除、未使用に

/* ===============================
   カスタムカラーパレット（落ち着いたオレンジ系）
   =============================== */

// 深いオレンジ（プライマリー）
private val BugMemoOrange = Color(0xFFD84315)
// ライトオレンジ
private val BugMemoOrangeLight = Color(0xFFFF7043)
// アクセントオレンジ
private val BugMemoAccent = Color(0xFFFF9800)
// 暖かみのある背景
private val BugMemoBackground = Color(0xFFFFF8F5)
// サーフェス色
private val BugMemoSurface = Color(0xFFFFFBFA)
// プライマリー上のテキスト
private val BugMemoOnPrimary = Color(0xFFFFFFFF)
// サーフェス上のテキスト
private val BugMemoOnSurface = Color(0xFF1C1B1F)

/* ===============================
   ライトテーマ
   =============================== */
private val LightColors = lightColorScheme(
    primary = BugMemoOrange,
    onPrimary = BugMemoOnPrimary,
    primaryContainer = BugMemoOrangeLight,
    onPrimaryContainer = Color(0xFF2D1600),
    secondary = BugMemoAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2D1600),
    tertiary = Color(0xFF6F5B40),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9DDBC),
    onTertiaryContainer = Color(0xFF271904),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BugMemoBackground,
    onBackground = Color(0xFF1F1B16),
    surface = BugMemoSurface,
    onSurface = BugMemoOnSurface,
    surfaceVariant = Color(0xFFF0E0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    outline = Color(0xFF817567),
    outlineVariant = Color(0xFFD3C4B4),
    scrim = Color.Black,
    inverseSurface = Color(0xFF34302A),
    inverseOnSurface = Color(0xFFF9EFE7),
    inversePrimary = Color(0xFFFFB599)
)

/* ===============================
   ダークテーマ
   =============================== */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB599),
    onPrimary = Color(0xFF4A1C00),
    primaryContainer = Color(0xFF6A2C00),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFE6C2A2),
    onSecondary = Color(0xFF422C15),
    secondaryContainer = Color(0xFF5B422A),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = Color(0xFFDCC1A0),
    onTertiary = Color(0xFF3E2D16),
    tertiaryContainer = Color(0xFF56442A),
    onTertiaryContainer = Color(0xFFF9DDBC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF16120E),
    onBackground = Color(0xFFF0E0D0),
    surface = Color(0xFF16120E),
    onSurface = Color(0xFFF0E0D0),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    outline = Color(0xFF9C8F80),
    outlineVariant = Color(0xFF4F4539),
    scrim = Color.Black,
    inverseSurface = Color(0xFFF0E0D0),
    inverseOnSurface = Color(0xFF34302A),
    inversePrimary = BugMemoOrange
)

/* ===============================
   テーマ適用
   - ★ Changed: SDK バージョン分岐を削除（minSdk=34 のため常に true 側になるチェックは不要）
   - ★ Added: dynamicDarkColorScheme / dynamicLightColorScheme を直接利用
   =============================== */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,                  // ★ Added: 呼び出し側で動的カラーのON/OFFを選べる
    content: @Composable () -> Unit
) {
    val context = LocalContext.current                // ★ Added: dynamic color に必要
    val colorScheme =
        if (useDynamicColor) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColors else LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
