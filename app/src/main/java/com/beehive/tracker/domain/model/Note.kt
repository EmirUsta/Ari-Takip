package com.beehive.tracker.domain.model

// Bir kovana ait tek zaman damgalı kayıt.
// tagColorHex ve tagLabel denormalize edilmiştir: etiket ileride silinse bile
// zaman çizelgesinde o andaki renk ve isim doğru görünmeye devam eder.
data class Note(
    val id: String,
    val hiveId: String,
    val tagId: String?,
    val tagColorHex: String?,   // Nota eklendiği andaki etiket rengi
    val tagLabel: String?,      // Nota eklendiği andaki etiket ismi
    val textContent: String?,
    val updateHiveColor: Boolean, // true ise bu notun etiketi kovana da atanır
    val createdAt: Long,
)
