// app/src/main/java/com/example/bugmemo/di/AppModule.kt
package com.example.bugmemo.di

import android.app.Application
import android.content.Context
import com.example.bugmemo.data.NotesRepository
import com.example.bugmemo.data.RoomNotesRepository
import com.example.bugmemo.data.db.AppDatabase
import com.example.bugmemo.data.prefs.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ───────────── Database ─────────────
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.get(context as Application)

    // ───────────── Repositories ─────────────

    // NotesRepository の実体を提供
    @Provides
    @Singleton
    fun provideNotesRepository(db: AppDatabase): NotesRepository = RoomNotesRepository(db.noteDao(), db.folderDao())

    // SettingsRepository を提供
    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository = SettingsRepository.get(context as Application)

    // ★ Future: MindMap 機能用のリポジトリがあればここに追加
    // @Provides
    // @Singleton
    // fun provideMindMapRepository(db: AppDatabase): MindMapRepository {
    //     return RoomMindMapRepository(db.mindMapDao())
    // }
}
