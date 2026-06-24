package com.beehive.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.LayoutMode

// Room veritabanı satırı. Apiary silinince CASCADE ile kovanlар da silinir.
// apiaryId sütununa index eklendi; belirli arılığa ait kovanlar sık sorgulandığı için önemli.
@Entity(
    tableName = "hive",
    foreignKeys = [
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiaryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("apiaryId")],
)
data class HiveEntity(
    @PrimaryKey val id: String,
    val apiaryId: String,
    val name: String,
    val posX: Float,
    val posY: Float,
    val layoutMode: String,         // Enum.name olarak saklanır; Room Enum converter gerektirmez
    val currentTagId: String?,
    val currentTagColorHex: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun HiveEntity.toDomain() = Hive(
    id = id,
    apiaryId = apiaryId,
    name = name,
    posX = posX,
    posY = posY,
    layoutMode = LayoutMode.valueOf(layoutMode),
    currentTagId = currentTagId,
    currentTagColorHex = currentTagColorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Hive.toEntity() = HiveEntity(
    id = id,
    apiaryId = apiaryId,
    name = name,
    posX = posX,
    posY = posY,
    layoutMode = layoutMode.name,
    currentTagId = currentTagId,
    currentTagColorHex = currentTagColorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
