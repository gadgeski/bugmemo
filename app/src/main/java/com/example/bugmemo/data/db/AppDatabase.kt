// app/src/main/java/com/example/bugmemo/data/db/AppDatabase.kt
package com.example.bugmemo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bugmemo.data.Folder
import com.example.bugmemo.data.Note

// ★ Changed: MindMapEntity を entities に追加
// ★ Changed: version を 1 -> 2 に上げる（テーブル追加のため）
@Database(
    entities = [Note::class, Folder::class, MindMapEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    // ★ Added: MindMapDao の取得メソッドを追加（これで AppModule のエラーが消えます）
    abstract fun mindMapDao(): MindMapDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bugmemo_db"
                )
                    // ★ Added: 開発中のため破壊的マイグレーションを許可（データが消えます）
                    // 本番リリース後は適切な Migration 定義が必要ですが、今はこれでOK
                    // ★ Fix: 引数 true を渡し、全テーブル削除を明示して警告を解消
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}