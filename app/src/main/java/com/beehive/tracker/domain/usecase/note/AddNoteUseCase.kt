package com.beehive.tracker.domain.usecase.note

import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.repository.NoteRepository
import java.util.UUID
import javax.inject.Inject

// Merkezi not kaydetme use case'i.
// updateHiveColor = true ise note kaydedildikten SONRA kovanın currentTagId'si güncellenir.
// Bu güncelleme Room Flow'unu tetikler → MapViewModel.hives StateFlow yeni değer yayar
// → MapScreen HivePinWidget rengini anında değiştirir. (Planın kritik reaktif zinciri)
class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val hiveRepository: HiveRepository,
) {
    suspend operator fun invoke(
        hiveId: String,
        tagId: String?,
        tagColorHex: String?,
        tagLabel: String?,
        textContent: String?,
        updateHiveColor: Boolean,
        noteId: String = UUID.randomUUID().toString(),
    ) {
        val note = Note(
            id = noteId,
            hiveId = hiveId,
            tagId = tagId,
            tagColorHex = tagColorHex,
            tagLabel = tagLabel,
            textContent = textContent,
            updateHiveColor = updateHiveColor,
            createdAt = System.currentTimeMillis(),
        )
        noteRepository.insert(note)

        if (updateHiveColor && tagId != null) {
            hiveRepository.updateCurrentTag(hiveId, tagId)
        }
    }
}
