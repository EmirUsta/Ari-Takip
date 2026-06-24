package com.beehive.tracker.domain.model

// Kullanıcı tanımlı global etiket. Herhangi bir kovana veya nota atanabilir.
// Renk "#RRGGBB" formatında tutulur; UI android.graphics.Color.parseColor ile çözer.
data class Tag(
    val id: String,
    val label: String,      // Kullanıcının görüp seçeceği metin: "Ana Arı Yok"
    val colorHex: String,   // Örn: "#E53935"
    val sortOrder: Int,     // Etiket listesindeki sıra; kullanıcı sürükleyerek değiştirebilir (Faz 2+)
    val createdAt: Long,
)
