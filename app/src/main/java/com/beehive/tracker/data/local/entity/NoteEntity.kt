package com.beehive.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beehive.tracker.domain.model.Note

// Kovan silinince CASCADE ile notlar da silinir.
// tagColorHex ve tagLabel denormalize; etiket silinse bile tarihi notlarda renk/isim kalır.
@Entity(
    tableName = "note",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("hiveId")],
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val hiveId: String,
    val tagId: String?,
    val tagColorHex: String?,
    val tagLabel: String?,
    val textContent: String?,
    val updateHiveColor: Boolean,
    val createdAt: Long,
)

fun NoteEntity.toDomain() = Note(
    id = id,
    hiveId = hiveId,
    tagId = tagId,
    tagColorHex = tagColorHex,
    tagLabel = tagLabel,
    textContent = textContent,
    updateHiveColor = updateHiveColor,
    createdAt = createdAt,
)

fun Note.toEntity() = NoteEntity(
    id = id,
    hiveId = hiveId,
    tagId = tagId,
    tagColorHex = tagColorHex,
    tagLabel = tagLabel,
    textContent = textContent,
    updateHiveColor = updateHiveColor,
    createdAt = createdAt,
)
