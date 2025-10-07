// app/src/main/java/com/example/bugmemo/data/prefs/SettingsRepository.kt
package com.example.bugmemo.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

class SettingsRepository private constructor(
    private val appContext: Context
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
            if (id == null) prefs.remove(Keys.filterFolderIdKey)
            else prefs[Keys.filterFolderIdKey] = id
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
    // デバッグ／テスト用途の全消し API（今は未使用）
    // ─────────────────────────────────────────────────────────────
    @Suppress("unused") // ★ Added: いまは未使用だが将来のデバッグ/テストで使うため保持
    suspend fun clearAll() { // ★ (変更点) 未使用警告を抑制するアノテーションを追加
        appContext.dataStore.edit { it.clear() }
    }

    companion object {
        @Volatile private var INSTANCE: SettingsRepository? = null
        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
