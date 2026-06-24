package com.beehive.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beehive.tracker.data.local.entity.HiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {

    // Flow döndürdüğü için Room, tablo her değiştiğinde otomatik yeni liste yayar.
    // MapScreen bu sayede DB yazma işlemini kendisi dinlemeden rengi günceller.
    @Query("SELECT * FROM hive WHERE apiaryId = :apiaryId ORDER BY createdAt ASC")
    fun observeByApiary(apiaryId: String): Flow<List<HiveEntity>>

    // HiveDetailScreen başlığı ve tag bilgisi için tek kovan izleyicisi
    @Query("SELECT * FROM hive WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<HiveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HiveEntity)

    // Sürükle-bırak sonrası konum kaydetme; sadece konum ve zaman güncellenir
    @Query("UPDATE hive SET posX = :posX, posY = :posY, updatedAt = :now WHERE id = :id")
    suspend fun updatePosition(id: String, posX: Float, posY: Float, now: Long = System.currentTimeMillis())

    // Nota etiket atanınca çağrılır. colorHex'i Tag tablosundan subquery ile çeker;
    // böylece Hive satırı her zaman kendi rengini taşır ve harita N+1 sorgu yapmaz.
    @Query("""
        UPDATE hive
        SET currentTagId = :tagId,
            currentTagColorHex = (SELECT colorHex FROM tag WHERE id = :tagId),
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun updateCurrentTag(id: String, tagId: String?, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM hive WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM hive WHERE apiaryId = :apiaryId ORDER BY createdAt ASC")
    suspend fun getAllByApiary(apiaryId: String): List<HiveEntity>
}
