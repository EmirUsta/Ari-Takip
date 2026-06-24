package com.beehive.tracker.domain.model

// Fiziksel bir kovana karşılık gelen domain modeli.
// currentTagId ve currentTagColorHex kasıtlı denormalizasyondur:
// harita ekranında her kovan için ayrı sorgu (N+1) yapılmadan
// doğrudan renk gösterilebilmesi sağlanır.
data class Hive(
    val id: String,
    val apiaryId: String,
    val name: String,
    val posX: Float,            // Harita tuvaline göre yatay piksel konumu
    val posY: Float,            // Harita tuvaline göre dikey piksel konumu
    val layoutMode: LayoutMode, // FREE = serbest yerleşim, GRID = ızgara hizalama
    val currentTagId: String?,          // Son atanan etiket — harita rengi buradan gelir
    val currentTagColorHex: String?,    // Etiketin hex rengi; Tag tablosu join'i olmadan okunur
    val createdAt: Long,
    val updatedAt: Long,
)
