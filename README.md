Salah is an offline prayer-learning Android app. Version 2.3 keeps the existing Persian Hanafi lessons and replaces the activity-per-page UI with a single Compose navigation host. Adhan uses a separate foreground service and lock-screen activity.

Build with JDK 21 (Java/Kotlin bytecode targets 17), Android SDK 36, Build Tools 35.0.0, and the checked-in Gradle 8.13 wrapper:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleRelease :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

The minimum Android version remains 21. Core-library desugaring supplies java.time on older devices. The Compose compiler plugin matches Kotlin 2.2.10; AGP is 8.10.1. Dependencies are resolved from Google Maven and Maven Central without machine-specific cache overrides. No Firebase account, Google Play Services, JitPack dependency or application secret is required. Release APKs need the existing production signing key before distribution; CI does not publish or sign production releases.

The user selects Persian or English UI on the first launch. Existing lesson bodies remain Persian and are explicitly labelled when using the English interface. Only the existing Hanafi teaching content is enabled. Other schools are modelled but remain unavailable until reviewed content is supplied. Teaching school, regional calculation method and Asr convention are separate settings.

Settings, bookmarks and reading progress use DataStore. Migration retains coordinates stored as either floats or double-bit longs, city, calculation method, and notification-only/automatic-adhan preferences from version 2.2. Original SharedPreferences are preserved. The application ID stays `com.androidhive.myapplication`.

Code responsibilities:

- `core/model`: persisted IDs and settings, independent of UI order.
- `domain/prayer`: one clock-aware, timezone-aware calculation engine used by UI and alarms.
- `domain/qibla`: bearing and rotation math without Android dependencies.
- `data`: versioned lesson catalog, cities and settings migration.
- `platform`: location/sensors, alarm delivery, notifications and audio lifecycles.
- `ui`: Compose navigation, accessible bilingual screens and reading controls.

The current catalog contains 21 lessons, 73 steps, the original audio associations and prayer illustrations. Search, bookmarks, step completion and resume use stable lesson/step IDs. Content provenance is retained from the previous app; this migration is not a new religious review.

Prayer displays update even when every alert is off. Alarm scheduling selects the next enabled prayer, excluding sunrise. Reboot, package update, clock/timezone changes and exact-alarm permission changes restore scheduling, including notification-only mode. Missing exact-alarm access uses an inexact fallback; the relevant settings explain possible delay. Android can prevent background foreground-service starts after inexact alarms, in which case the prayer notification remains available. Delivery and audio on physical devices, including vendor power-management policies, require device QA.

The Google Flow training page is intentionally empty of generated animation. Add the exported media later using [the video integration guide](docs/GOOGLE_FLOW_VIDEOS.md). It already has an offline video area, chapter selection and a link to the existing step-by-step prayer guide.

See [the implementation and QA notes](docs/IMPLEMENTATION_FA.md) for the change map and verification status. CI builds debug and shrunk release APKs, checks lint and unit tests, and runs UI regression tests on an accelerated Android 34 emulator. No publishing action is configured.
