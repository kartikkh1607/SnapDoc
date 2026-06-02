# SnapDoc

Spec-compliant ID, passport, and exam photos from a phone camera — without the studio trip.

The user picks a document (Aadhaar, PAN, passport, UPSC, visa, SSC…), the on-device pipeline takes care of background, face crop, dimensions, file size, and the per-document rules (head height %, eye line %, KB range, white/blue/grey background). The output is the JPEG the form actually accepts.

## Stack

- **UI** — Jetpack Compose, Material 3, type-safe Navigation
- **State** — MVI-ish ViewModel + `StateFlow`, Hilt DI
- **Camera** — CameraX (`LifecycleCameraController`)
- **ML** — ML Kit face detection + selfie segmentation, fully on-device
- **Storage** — DataStore preferences, JSON spec catalog in assets
- **Billing** — Google Play Billing (entitlement-gated print sheets / watermark removal)
- **Tests** — JUnit 4 + Robolectric + Truth (unit), Compose test rules (UI)

`minSdk` 24, `targetSdk` 35, Kotlin + KSP.

## Pipeline

```
   Camera capture (JPEG, EXIF-oriented)
            │
            ▼
   decodeOriented        — rotate by EXIF
            │
            ▼
   BackgroundRemover     — ML Kit selfie segmentation → alpha matte
            │
            ▼
   BackgroundCompositor  — fill with spec colour (white / blue / grey)
            │
            ▼
   FaceCropper           — ML Kit face + landmarks
                          → output rect from spec
                          (head-height %, eye-line %, target aspect)
            │
            ▼
   ImageResizer          — bilinear to spec px (e.g. 413×531 @ 300dpi)
            │
            ▼
   FileSizeCompressor    — binary-search JPEG quality to hit min/max KB
            │
            ▼
   SpecValidator         — head-height, eye-line, KB range, dims
            │
            ▼
   ProcessingResultStore — in-memory handoff to Preview / Export
```

Each stage emits a `ProcessingStage` to drive the progress UI. Failures (no face, head doesn't fit) surface as actionable messages, not crashes.

## Document specs

Specs live as JSON in `app/src/main/assets/specs/catalog_v1.json` — width/height in mm and px, DPI, head-height range, eye-line range, background colour, allowed file-size range, plus rules (glasses, expression, eyes open). Adding a new document = adding an entry, no code changes.

## Project layout

```
app/src/main/java/com/kartik/snapdoc/
├── data/
│   ├── specs/        — catalog loader + repository (search, filter, lookup)
│   ├── billing/      — Play Billing wrapper, entitlement state
│   └── prefs/        — DataStore-backed user prefs
├── domain/
│   ├── pipeline/     — the processing stages above
│   ├── camera/       — real-time face guidance analyzer
│   ├── print/        — multi-photo print-sheet layout
│   └── export/       — file/share output
└── ui/
    ├── navigation/   — type-safe routes
    ├── screens/      — splash, onboarding, home, camera, processing,
    │                   preview, export, print sheet, settings, doc detail
    └── components/   — reusable composables, theme
```

## Building

```
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Release builds need a `keystore.properties` at the repo root (see `keystore.properties.example`); without it, release falls back to debug signing and is *not* shippable.

## Tests

Unit tests cover the parts most likely to silently regress:
- `FileSizeCompressorTest` — binary-search lands in the requested KB range
- `PrintSheetLayoutTest` — 4×6 / A4 / 5×7 photo placement math
- `SpecCatalogJsonTest` — the asset catalog parses and is internally consistent
- `EntitlementStateTest` — purchase → entitlement transitions
- `FaceCropRectTest` — pure crop-math (face box + landmarks + spec → output rect)
