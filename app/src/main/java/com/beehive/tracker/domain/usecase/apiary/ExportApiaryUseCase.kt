package com.beehive.tracker.domain.usecase.apiary

import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.ApiaryRepository
import com.beehive.tracker.domain.repository.AudioRepository
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.repository.NoteRepository
import com.beehive.tracker.domain.repository.TagRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

// Arılığı + ses dosyalarını ZIP olarak dışa aktarır.
// ZIP yapısı: apiary.json + audio/*.m4a
class ExportApiaryUseCase @Inject constructor(
    private val apiaryRepo: ApiaryRepository,
    private val hiveRepo: HiveRepository,
    private val noteRepo: NoteRepository,
    private val tagRepo: TagRepository,
    private val audioRepo: AudioRepository,
) {
    suspend operator fun invoke(apiaryId: String, outputStream: OutputStream) {
        val apiary = apiaryRepo.getById(apiaryId)
            ?: error("Arılık bulunamadı: $apiaryId")

        val hives = hiveRepo.getAllByApiary(apiaryId)
        val tagIds = mutableSetOf<String>()

        data class NoteAudio(val note: Note, val audio: AudioRecord?)

        val hivesWithData = hives.map { hive ->
            hive.currentTagId?.let { tagIds.add(it) }
            val notes = noteRepo.getAllByHive(hive.id)
            val notesWithAudio = notes.map { note ->
                note.tagId?.let { tagIds.add(it) }
                NoteAudio(note, audioRepo.getByNoteId(note.id))
            }
            Pair(hive, notesWithAudio)
        }

        val tags = if (tagIds.isEmpty()) emptyList() else tagRepo.getByIds(tagIds.toList())
        val json = buildJson(apiary, tags, hivesWithData.map { (h, na) ->
            Triple(h, na.map { it.note }, na.mapNotNull { it.audio })
        })

        ZipOutputStream(outputStream.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("apiary.json"))
            zip.write(json.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            hivesWithData.forEach { (_, notesWithAudio) ->
                notesWithAudio.forEach { na ->
                    na.audio?.let { audio ->
                        val file = File(audio.filePath)
                        if (file.exists()) {
                            zip.putNextEntry(ZipEntry("audio/${file.name}"))
                            file.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            }
        }
    }

    private fun buildJson(
        apiary: Apiary,
        tags: List<Tag>,
        hivesWithData: List<Triple<Hive, List<Note>, List<AudioRecord>>>,
    ): String {
        val root = JSONObject()
        root.put("version", 2)

        root.put("apiary", JSONObject().apply {
            put("id", apiary.id)
            put("name", apiary.name)
            put("locationNote", apiary.locationNote)
            put("createdAt", apiary.createdAt)
            put("updatedAt", apiary.updatedAt)
        })

        root.put("tags", JSONArray().apply {
            tags.forEach { tag ->
                put(JSONObject().apply {
                    put("id", tag.id)
                    put("label", tag.label)
                    put("colorHex", tag.colorHex)
                    put("sortOrder", tag.sortOrder)
                    put("createdAt", tag.createdAt)
                })
            }
        })

        // audio listesini noteId → AudioRecord map'ine çevir
        val audioByNote = hivesWithData
            .flatMap { it.third }
            .associateBy { it.noteId }

        root.put("hives", JSONArray().apply {
            hivesWithData.forEach { (hive, notes, _) ->
                put(JSONObject().apply {
                    put("id", hive.id)
                    put("name", hive.name)
                    put("posX", hive.posX.toDouble())
                    put("posY", hive.posY.toDouble())
                    put("layoutMode", hive.layoutMode.name)
                    if (hive.currentTagId != null) put("currentTagId", hive.currentTagId)
                    if (hive.currentTagColorHex != null) put("currentTagColorHex", hive.currentTagColorHex)
                    put("createdAt", hive.createdAt)
                    put("updatedAt", hive.updatedAt)
                    put("notes", JSONArray().apply {
                        notes.forEach { note ->
                            put(JSONObject().apply {
                                put("id", note.id)
                                if (note.tagId != null) put("tagId", note.tagId)
                                if (note.tagColorHex != null) put("tagColorHex", note.tagColorHex)
                                if (note.tagLabel != null) put("tagLabel", note.tagLabel)
                                if (note.textContent != null) put("textContent", note.textContent)
                                put("updateHiveColor", note.updateHiveColor)
                                put("createdAt", note.createdAt)
                                audioByNote[note.id]?.let { ar ->
                                    put("audioRecord", JSONObject().apply {
                                        put("id", ar.id)
                                        put("fileName", File(ar.filePath).name)
                                        put("durationSeconds", ar.durationSeconds)
                                        if (ar.transcription != null) put("transcription", ar.transcription)
                                        put("transcriptionStatus", ar.transcriptionStatus.name)
                                        put("createdAt", ar.createdAt)
                                    })
                                }
                            })
                        }
                    })
                })
            }
        })

        return root.toString(2)
    }
}
