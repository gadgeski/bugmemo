@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui.screens

// ★ Added: 設定画面（言語切替のみ・最小）

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    // ★ Added: 現在の言語タグを購読（""=システム追従 / "ja" / "en"）
    val languageTag by AppLocaleManager.languageTagFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = "")
    var selected by remember(languageTag) { mutableStateOf(languageTag) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.title_settings)) })
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.pref_language),
                style = MaterialTheme.typography.titleMedium,
            )

            LanguageOptionRow(
                selected = selected == "",
                label = stringResource(R.string.pref_language_system),
                onClick = { selected = "" },
            )
            LanguageOptionRow(
                selected = selected == "ja",
                label = stringResource(R.string.pref_language_ja),
                onClick = { selected = "ja" },
            )
            LanguageOptionRow(
                selected = selected == "en",
                label = stringResource(R.string.pref_language_en),
                onClick = { selected = "en" },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBack,
                ) { Text(stringResource(R.string.action_close)) }

                Button(
                    onClick = {
                        scope.launch {
                            AppLocaleManager.setLanguage(ctx, selected)
                            // ★ Added: 反映のため必要なら Activity 再生成
                            activity?.recreate()
                        }
                    },
                    enabled = selected != languageTag,
                ) { Text(stringResource(R.string.action_apply)) }
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
