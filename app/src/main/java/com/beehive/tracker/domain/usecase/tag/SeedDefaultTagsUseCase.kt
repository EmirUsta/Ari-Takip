package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

// Uygulama ilk kurulduğunda etiket tablosu boşsa varsayılan etiketleri ekler.
// Application.onCreate'den çağrılır; ikinci açılışta "mevcut etiketler var" deyip hiçbir şey yapmaz.
class SeedDefaultTagsUseCase @Inject constructor(
    private val repository: TagRepository,
) {
    suspend operator fun invoke() {
        val existing = repository.observeAll().first()
        if (existing.isNotEmpty()) return

        val defaults = listOf(
            Triple("Ana Arı Yok",       "#E53935", 0),
            Triple("Bal Durumu İyi",     "#43A047", 1),
            Triple("Şurup Verilecek",    "#FDD835", 2),
            Triple("Kontrol Edildi",     "#1E88E5", 3),
            Triple("Hastalık Şüphesi",   "#FB8C00", 4),
        )

        val now = System.currentTimeMillis()
        defaults.forEach { (label, color, order) ->
            repository.insert(Tag(UUID.randomUUID().toString(), label, color, order, now))
        }
    }
}
