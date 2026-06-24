# Kovan Takip Uygulaması — Geliştirme Planı

## Proje Özeti
Arıcıların fiziksel kovan dizilimlerini görsel olarak haritalayabileceği, kovanlara renk/etiket atayabileceği ve sesli/yazılı notlar alarak kovan tarihçesini takip edebileceği, **tamamen çevrimdışı (offline-first)** Android uygulaması.

---

## Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Dil | Kotlin |
| UI | Jetpack Compose |
| Veritabanı | Room (SQLite) |
| Reaktif Akış | Kotlin Flow + StateFlow |
| Durum Yönetimi | ViewModel + StateFlow |
| Ses Kaydı | MediaRecorder |
| Ses Oynatma | MediaPlayer |
| Tuval / Harita | Compose Canvas + pointerInput |
| Bağımlılık Enjeksiyonu | Hilt |
| Asenkron | Coroutines |

---

## Veri Modelleri

### Apiary (Arılık)
```
id            : UUID
name          : String
locationNote  : String?
createdAt     : Timestamp
updatedAt     : Timestamp
```

### Hive (Kovan)
```
id               : UUID
apiary_id        : UUID → Apiary.id
name             : String
pos_x            : Float          // Harita X koordinatı
pos_y            : Float          // Harita Y koordinatı
layout_mode      : Enum(FREE, GRID)
current_tag_id   : UUID?          // Denormalize — harita rengi için
current_tag_color_hex : String?   // Denormalize — N+1 sorguyu önler
createdAt        : Timestamp
updatedAt        : Timestamp
```

### Tag (Etiket)
```
id         : UUID
label      : String       // "Ana Arı Yok", "Şurup Verilecek"
color_hex  : String       // "#E53935"
sort_order : Int
createdAt  : Timestamp
```

### Note (Not)
```
id                : UUID
hive_id           : UUID → Hive.id
tag_id            : UUID? → Tag.id
text_content      : String?
update_hive_color : Boolean
createdAt         : Timestamp
```

### AudioRecord (Ses Kaydı)
```
id                   : UUID
note_id              : UUID → Note.id   (1-1)
file_path            : String
duration_seconds     : Int
transcription        : String?          // STT sonucu; başlangıçta NULL
transcription_status : Enum(NONE, PENDING, DONE, FAILED)
createdAt            : Timestamp
```

### İlişki Şeması
```
Apiary ──< Hive ──< Note ──┤ Tag (global)
                    │
                    └──── AudioRecord (1-1)
```

---

## Mimari Yapı (Clean Architecture)

```
app/src/main/java/com/beehive/tracker/
│
├── core/
│   ├── di/            # Hilt modülleri (DatabaseModule, RepositoryModule)
│   └── theme/         # Renk, tema
│
├── domain/            # Framework-bağımsız iş mantığı
│   ├── model/         # Apiary, Hive, Tag, Note, AudioRecord
│   ├── repository/    # Soyut sözleşmeler (interface)
│   └── usecase/       # Tek sorumluluk iş kuralları
│       ├── apiary/
│       ├── hive/
│       ├── note/      # Faz 3'te eklenecek
│       ├── tag/
│       └── audio/     # Faz 5'te eklenecek
│
├── data/              # Room implementasyonları
│   ├── local/
│   │   ├── entity/    # Room @Entity sınıfları
│   │   ├── dao/       # Room @Dao arayüzleri
│   │   └── AppDatabase.kt
│   └── repository/    # Repository implementasyonları
│
└── presentation/
    ├── navigation/    # Screen + AppNavGraph
    ├── screen/
    │   ├── home/      # Arılık listesi
    │   ├── map/       # Kovan haritası (sürükle-bırak)
    │   ├── hivedetail/# Zaman çizelgesi (Faz 3)
    │   ├── noteeditor/# Not ekleme (Faz 3)
    │   └── settings/  # Etiket yönetimi (Faz 2)
    └── component/     # HivePinWidget, TimelineItem, AudioPlayer...
```

---

## Durum Yönetimi Veri Akışı

```
[Kullanıcı "Not Kaydet"e basar]
        │
        ▼
NoteEditorViewModel → AddNote UseCase
        │
        ▼
NoteRepository → Room'a Note yazar
        │
        ├─ update_hive_color=true ise
        └─ HiveRepository.updateCurrentTag(hiveId, tagId)
                │
                ▼
        Room Flow tetiklenir
                │
                ▼
MapViewModel.hives (StateFlow) otomatik güncellenir
                │
                ▼
MapScreen → HivePinWidget rengi anında değişir
```

---

## Geliştirme Fazları

### ✅ FAZ 1 — Temel Altyapı (TAMAMLANDI)
**Teslim Kriteri:** Arılık oluştur → kovanlara gir → kovan sürükle-bırak

- [x] Gradle + Hilt + Room + Compose kurulumu
- [x] Room entity'leri: `ApiaryEntity`, `HiveEntity`, `TagEntity`
- [x] DAO'lar: reaktif `Flow<List<T>>` sorgular
- [x] Repository sözleşmeleri (interface) ve implementasyonlar
- [x] Use Case'ler: `GetApiarys`, `CreateApiary`, `GetHivesByApiary`, `CreateHive`, `MoveHive`, `GetTags`, `CreateTag`
- [x] Navigasyon: Home → Map → HiveDetail
- [x] `HomeScreen`: arılık listesi + dialog ile yeni arılık ekleme
- [x] `MapScreen`: tuval üzerinde kovan pinleri, uzun bas → kovan ekle, sürükle-bırak
- [x] `HiveDetailScreen`: placeholder (Faz 3'te doldurulacak)
- [x] `HivePinWidget`: renk koduna göre dinamik pin
- [x] **Derleme: BUILD SUCCESSFUL** (0 hata, 3 uyarı — placeholder parametreler)

---

### ✅ FAZ 2 — Etiket Yönetimi ve Dinamik Renklendirme (TAMAMLANDI)
**Teslim Kriteri:** Ayarlardan etiket rengi değişince harita anında güncellenir

- [x] `SettingsScreen` + `SettingsViewModel`
- [x] Tag CRUD: ekle / sil / sırala
- [x] `TagChip` bileşeni
- [x] `MapScreen`'e ayarlar butonu
- [x] Uygulama ilk açılışta varsayılan etiket seed'i (`SeedDefaultTagsUseCase`)
- [x] `HivePinWidget` renk akışını `Tag` tablosuna bağla (`currentTagColorHex` denormalizasyon)

---

### ✅ FAZ 3 — Not Alma ve Zaman Çizelgesi (TAMAMLANDI)
**Teslim Kriteri:** Kovan detayında tarih sıralı not geçmişi; not eklenince harita rengi değişir

- [x] `Note` + `NoteEntity` + `NoteDao` + `NoteRepository`
- [x] `AddNote`, `GetNotesTimeline` use case'leri
- [x] `HiveDetailScreen`: dikey timeline view
- [x] `NoteEditorScreen`: yazı alanı + etiket seçici + "kovan rengini güncelle" toggle
- [x] `TimelineItem` bileşeni (tarih başlığı, etiket chip, metin)
- [x] Reaktif bağlantı: not kaydedilince `Hive.currentTagId` güncellenir → harita anında renklenir

---

### ✅ FAZ 4 — Gelişmiş Harita: Pan/Zoom + Grid Modu (TAMAMLANDI)
**Teslim Kriteri:** 10 kovan eklenip düzenlenebilir; pozisyonlar kalıcı

- [x] `Modifier.transformable` ile pan + zoom (edit modunda kilitli)
- [x] Grid modu: `ArrangeHivesInGridUseCase` ile 4 sütunlu otomatik hizalama
- [x] Kovan silme: `DeleteHiveUseCase` + uzun bas `DropdownMenu`
- [x] Haritada kovan sayısı rozeti (TopAppBar)

---

### ✅ FAZ 5 — Sesli Not Sistemi (TAMAMLANDI)
**Teslim Kriteri:** Nota ses kaydı eklenip oynatılabiliyor; uygulama yeniden başlayınca ses dosyası erişilebilir

- [x] `AudioRecord` domain modeli + `TranscriptionStatus` enum + `NoteWithAudio`
- [x] `AudioRepository` interface + `AudioRepositoryImpl`
- [x] `AudioRecordEntity` + `AudioRecordDao` + Room MIGRATION_2_3 (version 3)
- [x] `NoteWithAudioEntity` Room @Relation (LEFT JOIN note + audio_record)
- [x] `SaveAudioRecordUseCase` + `GetNotesWithAudioUseCase`
- [x] `RECORD_AUDIO` izin yönetimi (`rememberLauncherForActivityResult`)
- [x] `AudioRecorderController` (MediaRecorder sarmalayıcı) + `AudioPlayerController`
- [x] `NoteEditorScreen` kayıt/durdur/oynat/sil kontrolleri; kayıt sayacı
- [x] `AudioPlayerWidget` (timeline ve not editörde)
- [x] `HiveDetailViewModel` çoklu kayıt yönetimi (aynı anda tek oynatma)
- [x] `MediaRecorder` → `filesDir/audio/*.m4a` kayıt; not kaydedilmezse dosya temizlenir

---

### ✅ FAZ 9 — Grid Büyütme + Arılık Silme + ZIP Ses Aktarımı (TAMAMLANDI)
**Teslim Kriteri:** 2×2 hücre grid, 500 kovan desteği, arılık silme, ses dahil ZIP aktarımı

- [x] `core/constants/GridConstants.kt`: CELL=100f, SNAP=200f (2×CELL), COLS=4
- [x] `ArrangeHivesInGridUseCase`: SNAP aralığında (2×2 hücre) hizalama
- [x] `MapScreen.kt`: ince (CELL) + kalın (SNAP) çift katmanlı ızgara, 260 satır (500 kovan)
- [x] `MapScreen.kt` + `HivePinWidget.kt`: snapToGrid GridConstants.SNAP'a güncellendi
- [x] `DeleteApiaryUseCase` + `HomeViewModel.deleteApiary` + `HomeScreen` silme onay dialog
- [x] `AudioRecordDao/Repository/Impl`: `getByNoteId` eklendi
- [x] `ExportApiaryUseCase`: ZIP formatı (v2) — apiary.json + audio/*.m4a
- [x] `ImportApiaryUseCase`: ZIP (v2) ve eski JSON (v1) geri uyumlu; ses dosyaları filesDir'e kaydedilir; `@ApplicationContext` inject

---

### ✅ FAZ 8 — Arılık Dışa / İçe Aktarma (TAMAMLANDI)
**Teslim Kriteri:** Arılık JSON olarak kaydedilip geri yüklenebilir; ses kayıtları hariç tüm veri aktarılır

- [x] `ApiaryDao`, `HiveDao`, `NoteDao`, `TagDao`: suspend tek seferlik okuma metotları eklendi
- [x] `ApiaryRepository` + Impl: `getById` eklendi
- [x] `HiveRepository` + Impl: `getAllByApiary` eklendi
- [x] `NoteRepository` + Impl: `getAllByHive` eklendi
- [x] `TagRepository` + Impl: `getByIds` eklendi
- [x] `ExportApiaryUseCase`: arılık + kovan + not + etiket → JSON string (org.json)
- [x] `ImportApiaryUseCase`: JSON parse → DB insert (REPLACE; tag → apiary → hive → note sırası)
- [x] `HomeViewModel`: `export(id, outputStream)` + `import(json)` + `snackbarMessage` StateFlow
- [x] `HomeScreen`: üç nokta menüsü "Dışa Aktar" (SAF CreateDocument) + TopAppBar içe aktar butonu (SAF OpenDocument) + SnackbarHost

---

### ✅ FAZ 7 — Grid Snap Düzenleme Modu (TAMAMLANDI)
**Teslim Kriteri:** Edit modunda ızgara görünür; kovanlar sürüklenince hücreye yapışır

- [x] `MapScreen.kt`: `Canvas` ile amber renk ızgara çizimi (96f hücre, edit modunda)
- [x] `MapScreen.kt`: Uzun bas ile kovan ekleme `tapOffset` → `snapToGrid` ile hücreye hizalanır
- [x] `HivePinWidget.kt`: `rawOffset` + `displayOffset` iki state; sürükleme sırasında hücreler arasında zıplama
- [x] `snapToGrid` saf fonksiyon: `40f + round((x-40)/96) * 96`

---

### ✅ FAZ 6 — Stabilizasyon + STT Altyapısı (TAMAMLANDI)
**Teslim Kriteri:** Uygulama yayına hazır; STT bağlandığında mevcut sesler sıraya alınabilir

- [x] Faz 5 unit testleri: `SaveAudioRecordUseCaseTest` (5), `AudioRepositoryImplTest` (4), `GetNotesWithAudioUseCaseTest` (2)
- [x] `AudioRepository.updateStatus()` + `AudioRecordDao.updateStatus()` eklendi
- [x] `TranscriptionRemoteDataSource` interface + `NoOpTranscriptionDataSource` (Hilt'e @Binds)
- [x] `QueueTranscriptionUseCase` — kayıt PENDING işaretler; Worker bağlanınca tarayacak
- [x] MapScreen boş durum: kovan yokken edit/view moduna göre ipucu mesajı
- [x] Erişilebilirlik: tüm `Icon`'larda `contentDescription`; FAB ve IconButton açıklamalı

---

## Kritik Tasarım Kararları

| Karar | Gerekçe |
|---|---|
| `currentTagColorHex` Hive'da tutulur | Harita ekranında N+1 sorgu olmadan renk göstermek için denormalizasyon |
| `AudioRecord` ayrı tablo | Not → metin bozulmadan STT alanları ileride eklenir |
| DB Flow üzerinden iletişim | MapState ve NoteEditorState birbirini import etmez; bağımsız test edilebilir |
| `update_hive_color` toggle | Arıcı "bu kontrol nottur, rengi değiştirmesin" diyebilmeli |
| `transcription_status` enum | NONE/PENDING/DONE/FAILED → STT servisi olmadan uygulama kırılmaz |
| minSdk = 26 | MediaRecorder AAC tam desteği; adaptive icon yeterli; modern API'ler |
