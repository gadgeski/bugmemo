// app/src/main/java/com/example/bugmemo/data/db/AppDatabase.kt
package com.example.bugmemo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 2,
    exportSchema = true // 将来の自動マイグレーション用にスキーマを出力
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // ★ Changed: lint(property-naming) 回避のため小文字キャメルケースへ
        private val migration1to2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 既存データを壊さないために DEFAULT を付与＆ NOT NULL を維持
                db.execSQL(
                    """
                    ALTER TABLE notes 
                    ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bugmemo.db"
                )
                    // ★ Changed: リネーム後のマイグレーションを登録
                    .addMigrations(migration1to2)
                    // .fallbackToDestructiveMigration() // データ消去になるため原則使わない
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

/* ─────────────────────────────────────────────────────────────
   参考：次の変更に備えるテンプレ
   v2 → v3 で列名変更やテーブル追加をしたい場合の雛形
───────────────────────────────────────────────────────────── */
// private val migration2to3 = object : Migration(2, 3) {
//     override fun migrate(db: SupportSQLiteDatabase) {
//         // 例：新テーブルを追加
//         // db.execSQL("CREATE TABLE IF NOT EXISTS tags (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
//
//         // 例：列名変更は “新テーブル作成 → データコピー → 旧テーブル削除 → リネーム” が安全
//         // db.execSQL("CREATE TABLE notes_new ( ... )")
//         // db.execSQL("INSERT INTO notes_new (id, title, content, folderId, createdAt, updatedAt, isStarred)
//         //             SELECT id, title, content, folderId, createdAt, updatedAt, isStarred FROM notes")
//         // db.execSQL("DROP TABLE notes")
//         // db.execSQL("ALTER TABLE notes_new RENAME TO notes")
//     }
// }
// .addMigrations(migration1to2, migration2to3)
