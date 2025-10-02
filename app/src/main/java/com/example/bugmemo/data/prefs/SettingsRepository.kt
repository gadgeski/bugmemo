// app/src/main/java/com/example/bugmemo/data/prefs/SettingsRepository.kt
package com.example.bugmemo.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey   // ★ Added
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

class SettingsRepository private constructor(
    private val appContext: Context
) {
    private object Keys {
        val FILTER_FOLDER_ID = longPreferencesKey("filter_folder_id")
        val LAST_QUERY = stringPreferencesKey("last_query")         // ★ Added
    }

    val filterFolderId: Flow<Long?> =
        appContext.dataStore.data.map { prefs: Preferences ->
            prefs[Keys.FILTER_FOLDER_ID]
        }

    suspend fun setFilterFolderId(id: Long?) {
        appContext.dataStore.edit { prefs ->
            if (id == null) prefs.remove(Keys.FILTER_FOLDER_ID)
            else prefs[Keys.FILTER_FOLDER_ID] = id
        }
    }

    // ★ Added: 検索クエリの監視（空文字は空として保持）
    val lastQuery: Flow<String> =
        appContext.dataStore.data.map { prefs -> prefs[Keys.LAST_QUERY] ?: "" }

    // ★ Added: 検索クエリの保存
    suspend fun setLastQuery(q: String) {
        appContext.dataStore.edit { prefs -> prefs[Keys.LAST_QUERY] = q }
    }

    companion object {
        @Volatile private var INSTANCE: SettingsRepository? = null
        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
