@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bugmemo.R
import com.example.bugmemo.core.AppLocaleManager
import kotlinx.coroutines.launch
import kotlin.math.abs

// ★ Added: フォントサイズ用 Slider(androidx.compose.material3.Slider)
// ★ Added: すべての文言をリソース化(androidx.compose.ui.res.stringResource)
// ★ Added: strings.xml キー参照(com.example.bugmemo.R)
// ★ Added: フォントスケール比較用(kotlin.math.abs)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    // ★ keep: 言語タグを購読（""=システム追従 / "ja" / "en"）
    val languageTag by AppLocaleManager.languageTagFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = "")

    // ★ Added: エディタのフォントスケールを購読（1.0=等倍）
    val editorFontScale by AppLocaleManager.editorFontScaleFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    var selected by remember(languageTag) { mutableStateOf(languageTag) }
    // ★ Added: スライダーは Apply まで即保存しないため一時値を持つ
    var tempScale by rememberSaveable(editorFontScale) { mutableFloatStateOf(editorFontScale) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ★ Changed: タイトルをリソース化
                    Text(text = stringResource(R.string.title_settings))
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ===== 言語設定 =====
            // ★ Changed: セクション見出しをリソース化
            Text(
                text = stringResource(R.string.pref_language),
                style = MaterialTheme.typography.titleMedium,
            )

            LanguageOptionRow(
                selected = selected == "",
                label = stringResource(R.string.pref_language_system),
                // ★ Changed
                onClick = { selected = "" },
            )
            LanguageOptionRow(
                selected = selected == "ja",
                label = stringResource(R.string.pref_language_ja),
                // ★ Changed
                onClick = { selected = "ja" },
            )
            LanguageOptionRow(
                selected = selected == "en",
                label = stringResource(R.string.pref_language_en),
                // ★ Changed
                onClick = { selected = "en" },
            )

            // ===== エディタ文字サイズ =====
            // ★ Added: セクション見出しをリソース化
            Text(
                text = stringResource(R.string.pref_editor_font_size),
                style = MaterialTheme.typography.titleMedium,
            )
            // ★ Added: 現在値（例: 120%）をリソース化したフォーマットで表示
            Text(
                text = stringResource(
                    R.string.pref_editor_font_size_value,
                    (tempScale * 100).toInt(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            // ★ Added: スライダー（0.5x〜2.0x）
            Slider(
                value = tempScale,
                onValueChange = { tempScale = it.coerceIn(0.5f, 2.0f) },
                valueRange = 0.5f..2.0f,
            )

            // ===== アクション =====
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBack,
                ) {
                    // ★ Changed: 「閉じる」をリソース化
                    Text(stringResource(R.string.action_close))
                }

                Button(
                    onClick = {
                        scope.launch {
                            // ★ keep: 言語の適用
                            if (selected != languageTag) {
                                AppLocaleManager.setLanguage(ctx, selected)
                                // 言語は Activity 再生成が必要
                                activity?.recreate()
                            }
                            // ★ Added: フォントスケールの適用（即時反映；再生成は不要）
                            if (abs(tempScale - editorFontScale) > 0.0001f) {
                                AppLocaleManager.setEditorFontScale(ctx, tempScale)
                            }
                        }
                    },
                    // ★ Changed: どちらか変更があるときだけ有効化
                    enabled = (selected != languageTag) ||
                        (abs(tempScale - editorFontScale) > 0.0001f),
                ) {
                    // ★ Changed: 「適用」をリソース化
                    Text(stringResource(R.string.action_apply))
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
