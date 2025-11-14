// app/src/main/java/com/example/bugmemo/data/db/AppDatabase.kt
package com.example.bugmemo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, FolderEntity::class, NoteFts::class],
    version = 4,
    // ★ Changed: 3 → 4（FTS 定義を修正：content_rowid=id を追加）
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        // v1 → v2: isStarred 列の追加
        private val migration1to2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE notes
                    ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0
                    """.trimIndent(),
                )
            }
        }

        // v2 → v3: FTS（notesFts）と補助インデックスを整備
        // - 既存誤定義に備えて、古い FTS/トリガは DROP → 正しい定義で CREATE
        // - notes/folders のインデックスを IF NOT EXISTS で明示作成
        // - REBUILD で既存データを投入
        private val migration2to3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- notes の補助インデックス（Room の検証に合わせる） ---
                db.execSQL("""CREATE INDEX IF NOT EXISTS index_notes_folderId  ON notes(folderId)""")
                db.execSQL("""CREATE INDEX IF NOT EXISTS index_notes_updatedAt ON notes(updatedAt)""")
                db.execSQL("""CREATE INDEX IF NOT EXISTS index_notes_isStarred ON notes(isStarred)""")

                // --- folders テーブル & name インデックスの保険（存在しなければ作る） ---
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS folders(
                        id   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("""CREATE INDEX IF NOT EXISTS index_folders_name ON folders(name)""")

                // --- 既存の FTS/トリガを掃除（命名差分も含めて落とす） ---
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_BEFORE_UPDATE""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_BEFORE_DELETE""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_AFTER_INSERT""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_AFTER_UPDATE""")
                db.execSQL("""DROP TRIGGER IF EXISTS notes_ai""")
                db.execSQL("""DROP TRIGGER IF EXISTS notes_ad""")
                db.execSQL("""DROP TRIGGER IF EXISTS notes_au""")
                db.execSQL("""DROP TABLE   IF EXISTS notesFts""")
                // 再作成のため落とす

                // --- FTS4 外部コンテンツ（UNICODE61, content='notes'） ---
                db.execSQL(
                    """
                    CREATE VIRTUAL TABLE IF NOT EXISTS notesFts
                    USING fts4(
                        title,
                        content,
                        content='notes',
                        tokenize=unicode61
                    )
                    """.trimIndent(),
                )

                // --- 同期トリガ（external content は自動生成されない） ---
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_BEFORE_UPDATE
                    BEFORE UPDATE ON notes BEGIN
                        DELETE FROM notesFts WHERE docid=old.id;
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_BEFORE_DELETE
                    BEFORE DELETE ON notes BEGIN
                        DELETE FROM notesFts WHERE docid=old.id;
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_AFTER_INSERT
                    AFTER INSERT ON notes BEGIN
                        INSERT INTO notesFts(docid, title, content)
                        VALUES (new.id, new.title, new.content);
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_AFTER_UPDATE
                    AFTER UPDATE ON notes BEGIN
                        INSERT INTO notesFts(docid, title, content)
                        VALUES (new.id, new.title, new.content);
                    END
                    """.trimIndent(),
                )

                // --- 既存データの反映（外部コンテンツは REBUILD で content テーブルから再構築） ---
                db.execSQL("""INSERT INTO notesFts(notesFts) VALUES('rebuild')""")
            }
        }

        // ★ Added: v3 → v4
        // FTS を Room の期待に合わせて「content_rowid=id」を明示して再作成
        // 既存端末で v3 を踏んだ DB を安全に修復する
        private val migration3to4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- まず既存の FTS と関連トリガを削除 ---
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_BEFORE_UPDATE""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_BEFORE_DELETE""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_AFTER_INSERT""")
                db.execSQL("""DROP TRIGGER IF EXISTS notesFts_AFTER_UPDATE""")
                db.execSQL("""DROP TABLE   IF EXISTS notesFts""")

                // --- 正しい定義で作成（content_rowid=id を追加） ---
                db.execSQL(
                    """
                    CREATE VIRTUAL TABLE IF NOT EXISTS notesFts
                    USING fts4(
                        title,
                        content,
                        content='notes',
                        content_rowid=id,          -- ★ Added: 主キー列を明示（Room の期待に合わせる）
                        tokenize=unicode61
                    )
                    """.trimIndent(),
                )

                // --- トリガを再作成（docid=old.id / new.id で同期） ---
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_BEFORE_UPDATE
                    BEFORE UPDATE ON notes BEGIN
                        DELETE FROM notesFts WHERE docid=old.id;
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_BEFORE_DELETE
                    BEFORE DELETE ON notes BEGIN
                        DELETE FROM notesFts WHERE docid=old.id;
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_AFTER_INSERT
                    AFTER INSERT ON notes BEGIN
                        INSERT INTO notesFts(docid, title, content)
                        VALUES (new.id, new.title, new.content);
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notesFts_AFTER_UPDATE
                    AFTER UPDATE ON notes BEGIN
                        INSERT INTO notesFts(docid, title, content)
                        VALUES (new.id, new.title, new.content);
                    END
                    """.trimIndent(),
                )

                // --- 既存データで再構築（REBUILD） ---
                db.execSQL("""INSERT INTO notesFts(notesFts) VALUES('rebuild')""")
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bugmemo.db",
            )
                .addMigrations(
                    migration1to2,
                    migration2to3,
                    migration3to4,
                    // ★ Added: v3→v4 を登録
                )
                // .fallbackToDestructiveMigration() // 原則オフ（データ消去のため）
                .build()
                .also { instance = it }
        }
    }
}

/* ─────────────────────────────────────────────────────────────
   動作確認メモ:
   - 起動直後に "Migration didn't properly handle: notesFts(...)" が消えること
   - Database Inspector:
       PRAGMA index_list('notes');
       PRAGMA index_list('folders');
       SELECT count(*) FROM notesFts;
   - 既存端末（v3 適用済み）では v3→v4 が実行され、FTS の content_rowid=id が反映される
   - 新規インストール（v4 直作成）でも FTS とトリガが揃う
──────────────────────────────────────────────────────────── */
