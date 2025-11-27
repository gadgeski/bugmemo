// app/src/main/java/com/example/bugmemo/di/AppModule.kt
package com.example.bugmemo.di

import android.app.Application
import android.content.Context
import com.example.bugmemo.data.NotesRepository
import com.example.bugmemo.data.RoomNotesRepository
import com.example.bugmemo.data.db.AppDatabase
import com.example.bugmemo.data.db.MindMapDao
import com.example.bugmemo.data.prefs.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// ★ Fix: Hiltの生成コードからの参照をIDEが誤検知するのを防ぐために警告を抑制
@Suppress("unused")
object AppModule {

    // ───────────── Database ─────────────
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.get(context as Application)
    }

    // ★ Added: MindMapDao を提供
    @Provides
    @Singleton
    fun provideMindMapDao(db: AppDatabase): MindMapDao {
        return db.mindMapDao()
    }

    // ───────────── Repositories ─────────────

    @Provides
    @Singleton
    fun provideNotesRepository(db: AppDatabase): NotesRepository {
        return RoomNotesRepository(db.noteDao(), db.folderDao())
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository.get(context as Application)
    }
}