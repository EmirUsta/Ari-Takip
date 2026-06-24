# Kovan Takip Uygulaması — Proje Dokümantasyonu

> Oluşturulma: Haziran 2026  
> Platform: Android (minSdk 26)  
> Proje dizini: `~/Emir/mobil dizin app`  
> APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## İçindekiler

1. [Proje Amacı](#1-proje-amacı)
2. [Teknoloji Yığını](#2-teknoloji-yığını)
3. [Mimari Yapı](#3-mimari-yapı)
4. [Veritabanı Şeması](#4-veritabanı-şeması)
5. [Dosya Yapısı](#5-dosya-yapısı)
6. [Ekranlar ve Sorumlulukları](#6-ekranlar-ve-sorumlulukları)
7. [Harita ve Grid Sistemi](#7-harita-ve-grid-sistemi)
8. [Dışa / İçe Aktarma (ZIP v2)](#8-dışa--içe-aktarma-zip-v2)
9. [Ses Kaydı Sistemi](#9-ses-kaydı-sistemi)
10. [Reaktif Veri Akışı](#10-reaktif-veri-akışı)
11. [Kritik Tasarım Kararları](#11-kritik-tasarım-kararları)
12. [Geliştirme Fazları (Geçmişe Bakış)](#12-geliştirme-fazları-geçmişe-bakış)
13. [Derleme ve Yükleme](#13-derleme-ve-yükleme)

---

## 1. Proje Amacı

Arıcıların arılıklarındaki kovanları **görsel olarak haritalamasını** sağlayan, tamamen **çevrimdışı (offline-first)** Android uygulaması. Temel işlevler:

- Birden fazla arılık yönetimi
- Arılık haritasında kovan pinleri — sürükle-bırak ile konumlandırma
- Kovanlara renk-kodlu etiket atama (örn. "Ana Arı Yok", "Muayene Edildi")
- Kovan başına sesli ve yazılı not alma
- Tüm arılığı (kovan + notlar + sesler) dosyaya kaydet / başka cihaza aktar

---

## 2. Teknoloji Yığını

| Katman | Teknoloji | Notlar |
|---|---|---|
| Dil | Kotlin | |
| UI | Jetpack Compose | Declarative; Material 3 |
| Veritabanı | Room (SQLite) | `@Relation`, CASCADE, Migration |
| Reaktif Akış | Kotlin Flow + StateFlow | `SharingStarted.WhileSubscribed(5_000)` |
| Durum Yönetimi | ViewModel + StateFlow | `@HiltViewModel` |
| Bağımlılık Enjeksiyonu | Hilt | `@Binds`, `@ApplicationContext` |
| Ses Kaydı | MediaRecorder (AAC/M4A) | |
| Ses Oynatma | MediaPlayer | |
| Harita Tuval | Compose Canvas + `pointerInput` | `Modifier.transformable` |
| Asenkron | Kotlin Coroutines | `viewModelScope.launch` |
| JSON | `org.json` (Android SDK dahili) | Ekstra bağımlılık yok |
| ZIP | `java.util.zip` (JVM stdlib) | Ekstra bağımlılık yok |
| Dosya Erişimi | SAF (Storage Access Framework) | Depolama izni gerektirmez |

---

## 3. Mimari Yapı

**Clean Architecture** — 3 katman:

```
┌─────────────────────────────────────────────┐
│           Presentation (Compose UI)          │
│  Screen + ViewModel + Component + Navigation │
└──────────────────┬──────────────────────────┘
                   │ UseCase çağrısı
┌──────────────────▼──────────────────────────┐
│           Domain (İş Mantığı)               │
│  Model + Repository interface + UseCase      │
└──────────────────┬──────────────────────────┘
                   │ Repository impl
┌──────────────────▼──────────────────────────┐
│           Data (Room + Dosya Sistemi)        │
│  Entity + DAO + RepositoryImpl + AppDatabase │
└─────────────────────────────────────────────┘
```

**Temel prensipler:**
- Domain katmanı Android framework'e bağımlı değil (saf Kotlin sınıfları)
- ViewModel sadece UseCase'leri çağırır, DAO'yu doğrudan görmez
- Repository interface'leri domain'de tanımlı, impl'ler data katmanında
- Hilt tüm bağımlılıkları injection ile sağlar; `new` kullanılmaz

---

## 4. Veritabanı Şeması

**Sürüm:** 3 (Room `AppDatabase`)  
**Migrations:** `MIGRATION_1_2` + `MIGRATION_2_3` tanımlı

### Tablolar ve İlişkiler

```
apiary ──< hive ──< note ──< audio_record (1-1)
                      │
                      └─── tag (global, tüm arılıklarda ortak)
```

### apiary

| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | TEXT PK | UUID |
| `name` | TEXT | Arılık adı |
| `location_note` | TEXT | İsteğe bağlı konum notu |
| `created_at` | INTEGER | Unix ms |
| `updated_at` | INTEGER | Unix ms |

### hive

| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | TEXT PK | UUID |
| `apiary_id` | TEXT FK→apiary | CASCADE DELETE |
| `name` | TEXT | Kovan adı |
| `pos_x` | REAL | Harita X (virtual px) |
| `pos_y` | REAL | Harita Y (virtual px) |
| `layout_mode` | TEXT | `FREE` veya `GRID` |
| `current_tag_id` | TEXT? | Denormalize — son aktif etiket |
| `current_tag_color_hex` | TEXT? | Denormalize — N+1 önlemek için |
| `created_at` | INTEGER | |
| `updated_at` | INTEGER | |

**Not:** `current_tag_color_hex` harita ekranı performansı için denormalize tutulur. Not kaydedilip `update_hive_color=true` seçildiğinde otomatik güncellenir.

### tag

| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | TEXT PK | UUID |
| `label` | TEXT | "Ana Arı Yok" vb. |
| `color_hex` | TEXT | `"#E53935"` formatı |
| `sort_order` | INTEGER | Sıralama |
| `created_at` | INTEGER | |

### note

| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | TEXT PK | UUID |
| `hive_id` | TEXT FK→hive | CASCADE DELETE |
| `tag_id` | TEXT? FK→tag | İsteğe bağlı |
| `text_content` | TEXT? | Yazılı not |
| `update_hive_color` | INTEGER | Boolean: 1=rengi güncelle |
| `created_at` | INTEGER | |

### audio_record

| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | TEXT PK | UUID |
| `note_id` | TEXT FK→note | CASCADE DELETE; 1-1 ilişki |
| `file_path` | TEXT | `filesDir/audio/uuid.m4a` |
| `duration_seconds` | INTEGER | |
| `transcription` | TEXT? | STT sonucu (ileride) |
| `transcription_status` | TEXT | `NONE/PENDING/DONE/FAILED` |
| `created_at` | INTEGER | |

---

## 5. Dosya Yapısı

```
app/src/main/java/com/beehive/tracker/
│
├── BeeHiveApplication.kt           # @HiltAndroidApp giriş noktası
├── MainActivity.kt                 # @AndroidEntryPoint; AppNavGraph host
│
├── core/
│   ├── constants/
│   │   └── GridConstants.kt        # CELL=100f, SNAP=200f, COLS=4, START_X/Y=40f
│   ├── di/
│   │   ├── DatabaseModule.kt       # Room + DAO @Provides
│   │   └── RepositoryModule.kt     # @Binds interface→impl eşlemeleri
│   └── theme/
│       ├── Color.kt
│       └── Theme.kt
│
├── domain/
│   ├── model/
│   │   ├── Apiary.kt
│   │   ├── Hive.kt
│   │   ├── Tag.kt
│   │   ├── Note.kt
│   │   ├── AudioRecord.kt
│   │   ├── NoteWithAudio.kt        # Note + AudioRecord? birleşimi
│   │   ├── LayoutMode.kt           # enum: FREE, GRID
│   │   └── TranscriptionStatus.kt  # enum: NONE, PENDING, DONE, FAILED
│   │
│   ├── repository/                 # Soyut sözleşmeler (interface)
│   │   ├── ApiaryRepository.kt
│   │   ├── HiveRepository.kt
│   │   ├── NoteRepository.kt
│   │   ├── TagRepository.kt
│   │   └── AudioRepository.kt
│   │
│   └── usecase/
│       ├── apiary/
│       │   ├── GetApiarysUseCase.kt        # Flow<List<Apiary>>
│       │   ├── CreateApiaryUseCase.kt
│       │   ├── DeleteApiaryUseCase.kt      # CASCADE; tüm nested veri silinir
│       │   ├── ExportApiaryUseCase.kt      # ZIP v2: apiary.json + audio/*.m4a
│       │   └── ImportApiaryUseCase.kt      # ZIP v2 veya JSON v1 (geriye dönük)
│       ├── hive/
│       │   ├── GetHivesByApiaryUseCase.kt  # Flow<List<Hive>>
│       │   ├── CreateHiveUseCase.kt
│       │   ├── MoveHiveUseCase.kt          # posX, posY güncelle
│       │   ├── DeleteHiveUseCase.kt
│       │   └── ArrangeHivesInGridUseCase.kt # 4 sütun, SNAP=200f aralık
│       ├── note/
│       │   ├── AddNoteUseCase.kt           # update_hive_color ise tag'ı günceller
│       │   ├── GetNotesTimelineUseCase.kt  # Flow<List<Note>>, desc sıra
│       │   └── GetNotesWithAudioUseCase.kt # Flow<List<NoteWithAudio>>
│       ├── tag/
│       │   ├── GetTagsUseCase.kt
│       │   ├── CreateTagUseCase.kt
│       │   ├── DeleteTagUseCase.kt
│       │   └── SeedDefaultTagsUseCase.kt   # İlk açılışta varsayılan etiketler
│       └── audio/
│           ├── SaveAudioRecordUseCase.kt
│           └── QueueTranscriptionUseCase.kt # STT altyapısı (NoOp stub)
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # version=3, migrations
│   │   ├── entity/
│   │   │   ├── ApiaryEntity.kt     # + toDomain() / toEntity()
│   │   │   ├── HiveEntity.kt
│   │   │   ├── TagEntity.kt
│   │   │   ├── NoteEntity.kt
│   │   │   └── AudioRecordEntity.kt
│   │   └── dao/
│   │       ├── ApiaryDao.kt        # getAll():Flow, getById():suspend
│   │       ├── HiveDao.kt          # getByApiary():Flow, getAllByApiary():suspend
│   │       ├── NoteDao.kt          # getByHive():Flow, getAllByHive():suspend
│   │       ├── TagDao.kt           # getAll():Flow, getByIds():suspend
│   │       ├── AudioRecordDao.kt   # observeByNote():Flow, getByNoteId():suspend
│   │       └── NoteWithAudioEntity.kt  # @Relation LEFT JOIN
│   ├── repository/
│   │   ├── ApiaryRepositoryImpl.kt
│   │   ├── HiveRepositoryImpl.kt
│   │   ├── NoteRepositoryImpl.kt
│   │   ├── TagRepositoryImpl.kt
│   │   └── AudioRepositoryImpl.kt
│   └── remote/
│       ├── TranscriptionRemoteDataSource.kt  # interface (gelecek STT)
│       └── NoOpTranscriptionDataSource.kt    # @Binds stub
│
└── presentation/
    ├── navigation/
    │   ├── Screen.kt               # sealed class: Home, Map, HiveDetail, NoteEditor, Settings
    │   └── AppNavGraph.kt          # NavHost + composable rotaları
    │
    ├── screen/
    │   ├── home/
    │   │   ├── HomeScreen.kt       # Arılık listesi; dışa/içe aktar; sil
    │   │   └── HomeViewModel.kt
    │   ├── map/
    │   │   ├── MapScreen.kt        # Harita; grid; kovan pinleri
    │   │   └── MapViewModel.kt
    │   ├── hivedetail/
    │   │   ├── HiveDetailScreen.kt # Zaman çizelgesi; ses oynatma
    │   │   └── HiveDetailViewModel.kt
    │   ├── noteeditor/
    │   │   ├── NoteEditorScreen.kt # Yazılı + sesli not ekleme
    │   │   └── NoteEditorViewModel.kt
    │   └── settings/
    │       ├── SettingsScreen.kt   # Etiket CRUD
    │       └── SettingsViewModel.kt
    │
    ├── component/
    │   ├── HivePinWidget.kt        # Harita pini; rawOffset+displayOffset grid snap
    │   ├── TagChip.kt              # Renkli etiket chip
    │   ├── TimelineItemWidget.kt   # Zaman çizelgesi satırı
    │   └── AudioPlayerWidget.kt    # Oynat/durdur kontrolleri
    │
    └── audio/
        ├── AudioRecorderController.kt  # MediaRecorder sarmalayıcı
        └── AudioPlayerController.kt    # MediaPlayer sarmalayıcı
```

---

## 6. Ekranlar ve Sorumlulukları

### HomeScreen

**Navigasyon:** Giriş ekranı  
**ViewModel:** `HomeViewModel`

- Arılık listesi (`LazyColumn`); boş durum metni
- `+` FAB → yeni arılık dialog
- `⋮` menü (her arılık) → **Dışa Aktar** (SAF CreateDocument, `.zip`) | **Sil** (onay dialog + CASCADE)
- TopAppBar `FileUpload` ikonu → içe aktar (SAF OpenDocument; zip/json/*/* filtreleri)
- `SnackbarHost` → işlem sonucu geri bildirimi

### MapScreen

**Navigasyon:** `Home → Map(apiaryId)`  
**ViewModel:** `MapViewModel`

- Amber renk arkaplan (`0xFFF5F0E8`)
- `graphicsLayer` Box → `mapScale` + `mapOffset` dönüşümü (pan/zoom)
- `Modifier.transformable` — **sadece view modunda** etkin (`enabled = !isEditMode`)
- **Edit modunda** Canvas ızgara çizimi (bakınız §7)
- `HivePinWidget` foreach; `onDragEnd` → `MoveHiveUseCase`
- FAB `+` → kovan adı dialog (tapOffset varsayılan konum)
- Uzun bas (edit modunda) → `tapOffset = snapToGrid(raw)` → dialog
- TopAppBar: kovan sayısı rozeti | GridView ikonu (kovanları otomatik hizala) | Edit/Lock toggle | Settings

### HiveDetailScreen

**Navigasyon:** `Map → HiveDetail(hiveId)`  
**ViewModel:** `HiveDetailViewModel`

- Kovan adı başlık
- `LazyColumn` zaman çizelgesi: `TimelineItemWidget` (tarih, etiket chip, metin, `AudioPlayerWidget`)
- `+` FAB → `NoteEditor(hiveId)`
- `HiveDetailViewModel`: `activePlayingNoteId` StateFlow — aynı anda tek oynatma garantisi

### NoteEditorScreen

**Navigasyon:** `HiveDetail → NoteEditor(hiveId)`  
**ViewModel:** `NoteEditorViewModel`

- `OutlinedTextField` — yazılı not
- Etiket seçici (`FlowRow` TagChip)
- "Kovan rengini güncelle" toggle (`Switch`)
- Ses kaydı bölümü:
  - Kayıt yokken: Mikrofon butonu → `RECORD_AUDIO` izin iste → `AudioRecorderController.start()`
  - Kayıt devam ederken: saniye sayacı + Durdur butonu
  - Kayıt tamamlandıktan sonra: `AudioPlayerWidget` + Sil butonu
- Kaydet butonu → `AddNoteUseCase` → (update_hive_color=true ise) `HiveRepository.updateCurrentTag`

### SettingsScreen

**Navigasyon:** `Map → Settings` (TopAppBar)  
**ViewModel:** `SettingsViewModel`

- Etiket listesi (`LazyColumn`); renk kutusu + label metin + sil ikonu
- Yeni etiket ekle: label + Material renk seçici dialog

---

## 7. Harita ve Grid Sistemi

### Koordinat Sistemi

Tüm kovan konumları **virtual pixel** birimindedir — `Float (posX, posY)`.  
Room'a bu değerler doğrudan kaydedilir. Rendering'de `IntOffset(x.roundToInt(), y.roundToInt())` kullanılır.  
`mapScale` / `mapOffset` sadece görüntüleme dönüşümü içindir; DB değerleri etkilenmez.

### GridConstants (`core/constants/GridConstants.kt`)

```kotlin
object GridConstants {
    const val CELL   = 100f   // görsel ızgara hücre boyutu (px)
    const val SNAP   = 200f   // yapışma aralığı = 2×CELL; her kovan 2×2 hücre kaplar
    const val COLS   = 4      // ArrangeHivesInGridUseCase sütun sayısı
    const val START_X = 40f   // ızgara sol kenar boşluğu
    const val START_Y = 40f   // ızgara üst kenar boşluğu
}
```

### snapToGrid Fonksiyonu (aynı implementasyon hem MapScreen hem HivePinWidget'ta)

```kotlin
fun snapToGrid(raw: Offset): Offset = Offset(
    x = START_X + ((raw.x - START_X) / SNAP).roundToInt().coerceAtLeast(0) * SNAP,
    y = START_Y + ((raw.y - START_Y) / SNAP).roundToInt().coerceAtLeast(0) * SNAP,
)
```

Parmağın bırakıldığı noktanın en yakın SNAP hücresine yuvarlama (round-to-nearest); negatif olmaz (`coerceAtLeast(0)`).

### Sürükleme Sırasında Drift Önleme (HivePinWidget)

İki ayrı state kullanılır:

```kotlin
var rawOffset     by remember(posX, posY) { mutableStateOf(Offset(posX, posY)) }
var displayOffset by remember(posX, posY) { mutableStateOf(Offset(posX, posY)) }

// Sürükleme:
rawOffset += dragAmount          // parmağın gerçek birikimli konumu
displayOffset = snapToGrid(rawOffset)  // anlık hücre yapışması

// Bırakma:
onDragEnd(displayOffset.x, displayOffset.y)  // snaplanmış konum DB'ye yazılır
```

`rawOffset` olmadan `displayOffset += dragAmount` yapılırsa her hücre geçişinde parmak ile pin arasında kayma (drift) oluşurdu.

### Edit Modunda Canvas Izgara Çizimi

260 satır × 30 sütun = 500 kovana yeter (125 kovan satırı × 2 hücre/satır = 250 < 260).

```
İnce çizgiler (strokeWidth=1f, renk #33B8860B): CELL=100f aralığında — görsel yönlendirme
Kalın çizgiler (strokeWidth=2f, renk #88B8860B): SNAP=200f aralığında — kovan hücre sınırları
Hücre dolgusu: snap sınırlı hücreler #22B8860B, iç hücreler #0FB8860B
```

### ArrangeHivesInGridUseCase

Tüm kovanları `SNAP` aralığında 4 sütunlu ızgaraya oturtur:

```
col = index % COLS
row = index / COLS
posX = START_X + col * SNAP   →  40, 240, 440, 640 (px)
posY = START_Y + row * SNAP   →  40, 240, 440 ...
```

---

## 8. Dışa / İçe Aktarma (ZIP v2)

### Dışa Aktarma (ExportApiaryUseCase)

Girdi: `apiaryId: String, outputStream: OutputStream`

ZIP içeriği:
```
kovantakip-<arılık_adı>.zip
├── apiary.json     ← tüm metadata (JSON v2 formatı)
└── audio/
    ├── <uuid1>.m4a
    ├── <uuid2>.m4a
    └── ...
```

`apiary.json` yapısı (v2):
```json
{
  "version": 2,
  "apiary": { "id": "...", "name": "...", "locationNote": "...", "createdAt": 0 },
  "tags": [{ "id": "...", "label": "...", "colorHex": "#E53935", "sortOrder": 0 }],
  "hives": [
    {
      "id": "...", "name": "...", "posX": 40.0, "posY": 40.0,
      "layoutMode": "FREE", "currentTagId": null, "currentTagColorHex": null,
      "notes": [
        {
          "id": "...", "tagId": null, "textContent": "...",
          "updateHiveColor": false, "createdAt": 0,
          "audioRecord": {
            "id": "...", "fileName": "<uuid>.m4a",
            "durationSeconds": 12, "transcriptionStatus": "NONE"
          }
        }
      ]
    }
  ]
}
```

Ses dosyaları ZIP'e `audio/<fileName>` yoluyla eklenir. JSON içinde sadece `fileName` (tam yol değil) tutulur.

### İçe Aktarma (ImportApiaryUseCase)

Girdi: `inputStream: InputStream`

**Format tespiti:** İlk 4 byte okunur. `0x50 0x4B` (ZIP magic = "PK") ise ZIP; değilse eski JSON (v1).

**ZIP akışı:**
1. ZIP extract: `audio/*.m4a` → `context.filesDir/audio/` dizinine yazılır
2. `apiary.json` parse edilir
3. DB'ye ekleme sırası (FK kısıtı nedeniyle): `tag → apiary → hive → note → audio_record`
4. `INSERT OR REPLACE` stratejisi (aynı UUID tekrar import edilirse güncellenir)

**Geriye dönük uyumluluk:** v1 JSON (ses kayıtsız eski format) hâlâ çalışır. `"version"` alanı yoksa veya `1` ise ses kaydı eklenmez.

**`@ApplicationContext` ihtiyacı:** `filesDir` erişimi için `ImportApiaryUseCase` constructor'ına `@ApplicationContext Context` inject edilir.

---

## 9. Ses Kaydı Sistemi

### Depolama

Ses dosyaları `context.filesDir/audio/<uuid>.m4a` yoluna kaydedilir.  
SAF izni gerekmez; uygulama private dizinine yazılır.  
Not kaydedilmezse (`NoteEditorViewModel` temizleme) dosya silinir.  
Arılık silinince Room CASCADE → `audio_record` satırı silinir ancak `.m4a` dosyası diskte kalır (orphan). Uygulama verisi temizlenince silinir.

### AudioRecorderController

`MediaRecorder` sarmalayıcısı:
- `start(outputPath)` → `prepare()` + `start()`
- `stop()` → `stop()` + `release()` → süre hesaplanır
- `cancel()` → dosya silinir

Çıktı formatı: `OutputFormat.MPEG_4`, `AudioEncoder.AAC`

### AudioPlayerController

`MediaPlayer` sarmalayıcısı:
- `play(filePath, onComplete)` → `prepare()` + `start()`
- `pause()` / `resume()` / `stop()` / `release()`
- `HiveDetailViewModel.activePlayingNoteId`: aynı anda yalnızca bir ses oynatılır

### TranscriptionStatus

`NONE → PENDING → DONE/FAILED`

`QueueTranscriptionUseCase`: kaydı PENDING yapar. `NoOpTranscriptionDataSource` (Hilt'e `@Binds`) şu an stub; gerçek STT servisi bağlandığında yalnızca bu sınıf değişir.

---

## 10. Reaktif Veri Akışı

### Harita Renk Güncelleme Zinciri

```
[Kullanıcı "Kaydet" → update_hive_color=true]
        │
NoteEditorViewModel.saveNote()
        │
AddNoteUseCase → NoteRepository.insert(note)
        │
        └─ HiveRepository.updateCurrentTag(hiveId, tagId, colorHex)
                │
                Room Flow tetiklenir (hive tablosu değişti)
                │
MapViewModel.hives (StateFlow) → yeni emission
                │
MapScreen recompose → HivePinWidget rengi anında değişir
```

### StateFlow Yaşam Döngüsü

```kotlin
val hives = repository.getByApiary(apiaryId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

`WhileSubscribed(5_000)`: son subscriber ayrıldıktan 5 saniye sonra upstream Flow iptal edilir. Ekran rotasyonunda (yeniden subscribe) upstream yeniden başlamaz, değer korunur.

---

## 11. Kritik Tasarım Kararları

| Karar | Gerekçe |
|---|---|
| `currentTagColorHex` Hive'da denormalize | Harita ekranında her pin için ayrı Tag sorgusu (N+1) yapmadan anlık renk gösterimi |
| `rawOffset` + `displayOffset` ayrımı | Grid snap sırasında parmak ile pin arasında kayma (drift) önlenir |
| `AudioRecord` ayrı tablo | Not metni bozulmadan STT alanları (`transcription`, `status`) ileride eklenir |
| `TranscriptionStatus` enum | STT servisi olmadan uygulama kırılmaz; ileride sadece `NoOpTranscriptionDataSource` değişir |
| SAF kullanımı | `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` izni gerekmez; Android 10+ uyumlu |
| ZIP magic byte tespiti | Import'ta dosya uzantısına güvenmek yerine gerçek format doğrulaması |
| FK ekleme sırası (import) | Room FK kısıtları aktifken tag→apiary→hive→note→audio_record sırasıyla insert |
| `SeedDefaultTagsUseCase` | Yeni kurulumda etiket listesi boş gelmesin; arıcılığa özgü varsayılanlar |
| minSdk = 26 | MediaRecorder AAC tam desteği; modern Compose API'leri |

---

## 12. Geliştirme Fazları (Geçmişe Bakış)

| Faz | İçerik | Durum |
|---|---|---|
| **Faz 1** | Temel altyapı: Gradle/Hilt/Room/Compose, entity'ler, DAO'lar, HomeScreen, MapScreen (sürükle-bırak) | ✅ |
| **Faz 2** | Etiket yönetimi: SettingsScreen, Tag CRUD, varsayılan seed, denormalize renk akışı | ✅ |
| **Faz 3** | Not sistemi: Note modeli, HiveDetailScreen zaman çizelgesi, NoteEditorScreen, renk güncelleme tetikleyicisi | ✅ |
| **Faz 4** | Gelişmiş harita: pan/zoom (Modifier.transformable), ArrangeHivesInGrid, kovan silme | ✅ |
| **Faz 5** | Sesli notlar: AudioRecord, MediaRecorder/Player, izin yönetimi, AudioPlayerWidget | ✅ |
| **Faz 6** | Stabilizasyon + STT altyapısı: unit testler, updateStatus DAO, NoOpTranscriptionDataSource, erişilebilirlik | ✅ |
| **Faz 7** | Grid snap: Canvas ızgara, rawOffset+displayOffset, snapToGrid, uzun bas → hücreye kovan ekleme | ✅ |
| **Faz 8** | Dışa/içe aktarma (JSON v1): SAF launcher, HomeScreen 3-nokta menü, SnackbarHost | ✅ |
| **Faz 9** | Grid büyütme (2×2 hücre, 500 kovan), arılık silme (CASCADE), ZIP+ses aktarımı (v2 format) | ✅ |

---

## 13. Derleme ve Yükleme

### Gereksinimler

- Android Studio (Hedgehog veya üstü) **veya** komut satırı JDK 17+
- `local.properties` içinde `sdk.dir` tanımlı

### Debug APK

```bash
cd ~/Emir/mobil\ dizin\ app
./gradlew assembleDebug
```

Çıktı: `app/build/outputs/apk/debug/app-debug.apk`

### Cihaza Yükleme (ADB)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (imzalı)

```bash
./gradlew assembleRelease
# Keystore yapılandırması app/build.gradle.kts signingConfigs bloğuna eklenmeli
```

### Temizle

```bash
./gradlew clean
```

### Logcat (uygulama çalışırken)

```bash
adb logcat -s BeeHive:D AndroidRuntime:E
```

---

## Ek Notlar

- **Ses dosyası temizliği:** Arılık silinince Room CASCADE audio_record satırlarını siler, ancak `filesDir/audio/*.m4a` dosyaları diskte kalır (orphan). Kabul edilebilir; kullanıcı Ayarlar > Uygulama > Veriyi Temizle ile silebilir.
- **Çoklu arılık desteği:** Tag tablosu globaldir — tüm arılıklar aynı etiket havuzunu paylaşır.
- **STT altyapısı hazır:** `TranscriptionRemoteDataSource` interface'i ve `QueueTranscriptionUseCase` yazılı; gerçek STT API'si bağlandığında sadece `NoOpTranscriptionDataSource` yerine gerçek impl Hilt'e bağlanır.
- **Koordinat birimi tutarlılığı:** DB → ViewModel → Composable arasında float px değerleri değişmeden akar; `IntOffset` sadece Compose rendering'inde `roundToInt()` ile kullanılır.
