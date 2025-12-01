// app/src/main/java/com/example/bugmemo/data/prefs/SettingsRepository.kt
package com.example.bugmemo.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val DS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

class SettingsRepository private constructor(
    private val appContext: Context,
    // ★ Changed: 複数行パラメータなので末尾カンマを付ける（ktlint/spotless 推奨）
) {
    // キーは lowerCamel に（ktlint 対応）
    private object Keys {
        val filterFolderIdKey = longPreferencesKey("filter_folder_id")
        val lastQueryKey = stringPreferencesKey("last_query")
    }

    /** 現在のフォルダ絞り込みID（なければ null） */
    val filterFolderId: Flow<Long?> =
        appContext.dataStore.data.map { prefs: Preferences ->
            prefs[Keys.filterFolderIdKey]
        }

    /** フォルダ絞り込みIDを保存（null で削除） */
    suspend fun setFilterFolderId(id: Long?) {
        appContext.dataStore.edit { prefs ->
            // ★ Changed: 1行 if/else をブロックに展開（読みやすさルール）
            if (id == null) {
                prefs.remove(Keys.filterFolderIdKey)
            } else {
                prefs[Keys.filterFolderIdKey] = id
            }
        }
    }

    /** 検索クエリ（空文字をデフォルトで返す） */
    val lastQuery: Flow<String> =
        appContext.dataStore.data.map { prefs -> prefs[Keys.lastQueryKey] ?: "" }

    /** 検索クエリを保存 */
    suspend fun setLastQuery(q: String) {
        appContext.dataStore.edit { prefs -> prefs[Keys.lastQueryKey] = q }
    }

    // ─────────────────────────────────────────────────────────────
    // GitHub Token (EncryptedSharedPreferences)
    // ─────────────────────────────────────────────────────────────
    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        appContext,
        "secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _githubToken = MutableStateFlow(encryptedPrefs.getString("github_token", "") ?: "")
    val githubToken = _githubToken.asStateFlow()

    suspend fun setGithubToken(token: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit().putString("github_token", token).apply()
            _githubToken.value = token
        }
    }

    // ─────────────────────────────────────────────────────────────
    // デバッグ／テスト用途の全消し API（今は未使用）
    // ─────────────────────────────────────────────────────────────
    @Suppress("unused")
    // ★ Kept: 将来用に残す（未使用警告は抑制）
    suspend fun clearAll() {
        appContext.dataStore.edit { it.clear() }
    }

    companion object {
        // ★ Changed: ktlint(property-naming) 対応のため INSTANCE → instance にリネーム
        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository = // ★ Changed: '=' の行内にコメントを寄せる（改行位置を修正）
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}
