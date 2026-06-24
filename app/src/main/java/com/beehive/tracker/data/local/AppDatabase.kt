package com.beehive.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.beehive.tracker.data.local.dao.ApiaryDao
import com.beehive.tracker.data.local.dao.AudioRecordDao
import com.beehive.tracker.data.local.dao.HiveDao
import com.beehive.tracker.data.local.dao.NoteDao
import com.beehive.tracker.data.local.dao.TagDao
import com.beehive.tracker.data.local.entity.ApiaryEntity
import com.beehive.tracker.data.local.entity.AudioRecordEntity
import com.beehive.tracker.data.local.entity.HiveEntity
import com.beehive.tracker.data.local.entity.NoteEntity
import com.beehive.tracker.data.local.entity.TagEntity

// Faz 3: version 1→2, NoteEntity tablosu eklendi.
// Faz 5: version 2→3, AudioRecordEntity tablosu eklendi.
@Database(
    entities = [
        ApiaryEntity::class,
        HiveEntity::class,
        TagEntity::class,
        NoteEntity::class,
        AudioRecordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun tagDao(): TagDao
    abstract fun noteDao(): NoteDao
    abstract fun audioRecordDao(): AudioRecordDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS note (
                id TEXT PRIMARY KEY NOT NULL,
                hiveId TEXT NOT NULL,
                tagId TEXT,
                tagColorHex TEXT,
                tagLabel TEXT,
                textContent TEXT,
                updateHiveColor INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (hiveId) REFERENCES hive(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_note_hiveId ON note(hiveId)")
    }
}

// Version 2→3: audio_record tablosunu ekler; mevcut apiary/hive/tag/note verileri korunur.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS audio_record (
                id TEXT PRIMARY KEY NOT NULL,
                noteId TEXT NOT NULL,
                filePath TEXT NOT NULL,
                durationSeconds INTEGER NOT NULL,
                transcription TEXT,
                transcriptionStatus TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (noteId) REFERENCES note(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_audio_record_noteId ON audio_record(noteId)")
    }
}
