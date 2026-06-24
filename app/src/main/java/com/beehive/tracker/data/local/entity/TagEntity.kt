package com.beehive.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.beehive.tracker.domain.model.Tag

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey val id: String,
    val label: String,
    val colorHex: String,
    val sortOrder: Int,
    val createdAt: Long,
)

fun TagEntity.toDomain() = Tag(id, label, colorHex, sortOrder, createdAt)
fun Tag.toEntity() = TagEntity(id, label, colorHex, sortOrder, createdAt)
