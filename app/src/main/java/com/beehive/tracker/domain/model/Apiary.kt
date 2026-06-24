package com.beehive.tracker.domain.model

// Bir arılık lokasyonunu temsil eder. GPS yerine serbest metin kullanılır;
// uygulama tamamen offline çalıştığı için coğrafi koordinat gerekmez.
data class Apiary(
    val id: String,
    val name: String,
    val locationNote: String,   // "Kuzey bahçesi", "Dağ evi arkası" gibi serbest not
    val createdAt: Long,
    val updatedAt: Long,
)
