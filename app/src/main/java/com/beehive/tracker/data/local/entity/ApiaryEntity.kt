package com.beehive.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.beehive.tracker.domain.model.Apiary

@Entity(tableName = "apiary")
data class ApiaryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val locationNote: String,
    val createdAt: Long,
    val updatedAt: Long,
)

fun ApiaryEntity.toDomain() = Apiary(id, name, locationNote, createdAt, updatedAt)
fun Apiary.toEntity() = ApiaryEntity(id, name, locationNote, createdAt, updatedAt)
