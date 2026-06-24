package com.beehive.tracker.domain.usecase.apiary

import android.content.Context
import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.LayoutMode
import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.model.TranscriptionStatus
import com.beehive.tracker.domain.repository.ApiaryRepository
import com.beehive.tracker.domain.repository.AudioRepository
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.repository.NoteRepository
import com.beehive.tracker.domain.repository.TagRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

// ZIP (v2) veya sade JSON (v1) dosyasını içe aktarır.
// ZIP formatı: apiary.json + audio/*.m4a
class ImportApiaryUseCase @Inject constructor(
    private val apiaryRepo: ApiaryRepository,
    private val hiveRepo: HiveRepository,
    private val noteRepo: NoteRepository,
    private val tagRepo: TagRepository,
    private val audioRepo: AudioRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(inputStream: InputStream) {
        val buffered = inputStream.buffered()
        buffered.mark(4)
        val header = ByteArray(4)
        buffered.read(header)
        buffered.reset()

        if (header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()) {
            // PK magic → ZIP formatı (v2)
            importZip(buffered)
        } else {
            // Düz JSON (v1 — eski format geriye dönük uyumluluk)
            importJson(buffered.bufferedReader().readText(), emptyMap())
        }
    }

    private suspend fun importZip(inputStream: InputStream) {
        val audioFiles = mutableMapOf<String, ByteArray>()
        var jsonContent: String? = null

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == "apiary.json" -> jsonContent = zip.bufferedReader().readText()
                    entry.name.startsWith("audio/") -> {
                        val name = entry.name.removePrefix("audio/")
                        audioFiles[name] = zip.readBytes()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val json = jsonContent ?: error("ZIP içinde apiary.json bulunamadı")

        // Ses dosyalarını cihaza kaydet
        val audioDir = File(context.filesDir, "audio").also { it.mkdirs() }
        val savedPaths = audioFiles.mapValues { (name, bytes) ->
            File(audioDir, name).also { it.writeBytes(bytes) }.absolutePath
        }

        importJson(json, savedPaths)
    }

    private suspend fun importJson(json: String, audioPaths: Map<String, String>) {
        val root = JSONObject(json)
        val version = root.getInt("version")
        check(version in 1..2) { "Desteklenmeyen dosya versiyonu: $version" }

        val tagsArray = root.getJSONArray("tags")
        for (i in 0 until tagsArray.length()) {
            val t = tagsArray.getJSONObject(i)
            tagRepo.insert(Tag(
                id = t.getString("id"),
                label = t.getString("label"),
                colorHex = t.getString("colorHex"),
                sortOrder = t.getInt("sortOrder"),
                createdAt = t.getLong("createdAt"),
            ))
        }

        val a = root.getJSONObject("apiary")
        val apiary = Apiary(
            id = a.getString("id"),
            name = a.getString("name"),
            locationNote = a.optString("locationNote", ""),
            createdAt = a.getLong("createdAt"),
            updatedAt = a.getLong("updatedAt"),
        )
        apiaryRepo.insert(apiary)

        val hivesArray = root.getJSONArray("hives")
        for (i in 0 until hivesArray.length()) {
            val h = hivesArray.getJSONObject(i)
            val hive = Hive(
                id = h.getString("id"),
                apiaryId = apiary.id,
                name = h.getString("name"),
                posX = h.getDouble("posX").toFloat(),
                posY = h.getDouble("posY").toFloat(),
                layoutMode = LayoutMode.valueOf(h.optString("layoutMode", LayoutMode.FREE.name)),
                currentTagId = h.optString("currentTagId").takeIf { it.isNotEmpty() },
                currentTagColorHex = h.optString("currentTagColorHex").takeIf { it.isNotEmpty() },
                createdAt = h.getLong("createdAt"),
                updatedAt = h.getLong("updatedAt"),
            )
            hiveRepo.insert(hive)

            val notesArray = h.getJSONArray("notes")
            for (j in 0 until notesArray.length()) {
                val n = notesArray.getJSONObject(j)
                val noteId = n.getString("id")
                noteRepo.insert(Note(
                    id = noteId,
                    hiveId = hive.id,
                    tagId = n.optString("tagId").takeIf { it.isNotEmpty() },
                    tagColorHex = n.optString("tagColorHex").takeIf { it.isNotEmpty() },
                    tagLabel = n.optString("tagLabel").takeIf { it.isNotEmpty() },
                    textContent = n.optString("textContent").takeIf { it.isNotEmpty() },
                    updateHiveColor = n.optBoolean("updateHiveColor", false),
                    createdAt = n.getLong("createdAt"),
                ))

                if (!n.isNull("audioRecord") && version >= 2) {
                    val ar = n.getJSONObject("audioRecord")
                    val fileName = ar.getString("fileName")
                    val filePath = audioPaths[fileName]
                    if (filePath != null) {
                        audioRepo.insert(AudioRecord(
                            id = ar.getString("id"),
                            noteId = noteId,
                            filePath = filePath,
                            durationSeconds = ar.getInt("durationSeconds"),
                            transcription = ar.optString("transcription").takeIf { it.isNotEmpty() },
                            transcriptionStatus = TranscriptionStatus.valueOf(
                                ar.optString("transcriptionStatus", TranscriptionStatus.NONE.name)
                            ),
                            transcriptionVosk = ar.optString("transcriptionVosk").takeIf { it.isNotEmpty() },
                            transcriptionWhisper = ar.optString("transcriptionWhisper").takeIf { it.isNotEmpty() },
                            createdAt = ar.getLong("createdAt"),
                        ))
                    }
                }
            }
        }
    }
}
