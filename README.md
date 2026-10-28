# Salah 2.7

A Kotlin / Jetpack Compose prayer reference and utility app with continuous bookmarked topics, offline prayer times and qibla, ten languages, four Sunni schools, selectable adhan recordings and Ramadan suhur/iftar reminders.

**Downloads:** open the latest successful [Android Actions run](https://github.com/nemanaldin/salah-uncle/actions/workflows/android.yml) for `uncle-work`, sign in to GitHub, and download **salah-downloads** under **Artifacts**. It contains the installable debug APK, unsigned release APK, source ZIP, offline Persian HTML report and Android screenshots. Extract the archive first; install `salah-2.7.0-debug.apk`. The release APK needs the owner's signing key. Development signing keys can differ between builds.

[گزارش فارسی نسخهٔ ۲٫۷](docs/UPDATE_2_7_FA.md) · [Translation coverage](docs/TRANSLATION_COVERAGE.json) · [Offline cities and licence](docs/GEOGRAPHY.md) · [Adhan audio and rights](docs/ADHAN_RECORDINGS.md)

Topics open as complete continuous articles with stable star bookmarks; no next-step or completion UI is shown. Existing progress data is retained during upgrades. All 21 legacy Hanafi topics / 73 source sections have explanatory bodies in all ten languages, with Arabic recitations preserved. The photographic UI restores artwork from the owner’s original version. See the source-compared editorial corrections and pending independent native/specialist review in the 2.6 report. The new prayer/wudu introductions and shared post-wudu remembrance have text in all ten languages. Text availability does not claim specialist/native-language editorial approval. User-supplied audio can be imported for offline use; no newly licensed recording is bundled in this change.

Build with JDK 21 (Java/Kotlin bytecode targets 17), Android SDK 36, Build Tools 35.0.0, and the checked-in Gradle 8.13 wrapper:

```sh
python3 scripts/build_lesson_translations.py
python3 scripts/verify_content.py
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleRelease :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

The minimum Android version remains 21. Core-library desugaring supplies java.time on older devices. The Compose compiler plugin matches Kotlin 2.2.10; AGP is 8.10.1. Dependencies are resolved from Google Maven and Maven Central without machine-specific cache overrides. No Firebase account, Google Play Services, JitPack dependency or application secret is required. Release APKs need the existing production signing key before distribution; CI does not publish or sign production releases.

The first launch and settings support Persian, English, Arabic, Turkish, Urdu, Indonesian, Bengali, Malay, Pashto and French. Each of the four schools has its own localized introductory prayer and wudu guide with source links. Jafari is outside the requested scope. The longer Hanafi lessons also have all nine non-Persian body translations; these rulings are not reused under another school. The archived original Persian file remains intact, with sourced corrections in a separate runtime layer. Choosing a school sets the default Asr convention and preserves the regional Fajr/Isha method; the user may still adjust these settings separately. See [content scope and sources](docs/CONTENT_SOURCES.md).

Settings, bookmarks and reading progress use DataStore. Migration retains coordinates stored as either floats or double-bit longs, city, calculation method, and notification-only/automatic-adhan preferences from version 2.2. Original SharedPreferences are preserved. The application ID stays `com.androidhive.myapplication`.

Code responsibilities:

- `core/model`: persisted IDs and settings, independent of UI order.
- `domain/prayer`: one clock-aware, timezone-aware calculation engine used by UI and alarms.
- `domain/qibla`: bearing and rotation math without Android dependencies.
- `data`: versioned lesson catalog, cities and settings migration.
- `platform`: location/sensors, alarm delivery, notifications and audio lifecycles.
- `ui`: Compose navigation, localized RTL/LTR screens and reading controls.

The catalog contains the original 21 Hanafi lessons and 73 steps, plus 8 new guides with 80 language variants and 560 localized steps. Search, bookmarks, step completion and resume use stable lesson/step IDs. School-specific guides never fall back to another school's content. Source attribution is visible in the reader and the selected school's Sources page; specialist and native-language release reviews have not been claimed.

Prayer displays update even when every alert is off. Alarm scheduling selects the next enabled prayer, excluding sunrise. Reboot, package update, clock/timezone changes and exact-alarm permission changes restore scheduling, including notification-only mode. Missing exact-alarm access uses an inexact fallback; the relevant settings explain possible delay. Android can prevent background foreground-service starts after inexact alarms, in which case the prayer notification remains available. Delivery and audio on physical devices, including vendor power-management policies, require device QA.

The Google Flow training page is intentionally empty of generated animation. Add the exported media later using [the video integration guide](docs/GOOGLE_FLOW_VIDEOS.md). It already has an offline video area, chapter selection and a link to the existing step-by-step prayer guide.

See [the 2.4 language and school notes](docs/LANGUAGES_SCHOOLS_FA.md), [content sources](docs/CONTENT_SOURCES.md), and [the original 2.3 implementation notes](docs/IMPLEMENTATION_FA.md). CI validates content, builds debug and shrunk release APKs, checks lint and unit tests, and runs UI regression tests on an accelerated Android 34 emulator, including screenshots of all ten interface languages. No publishing action is configured.

Ramadan uses the existing Fajr/Maghrib engine and an explicitly estimated, adjustable Umm al-Qura month. Suhur defaults to 90 minutes before Fajr, independently of adhan alerts, and requires exact-alarm and notification access before scheduling. Audio uses the system alarm stream with stop controls and a bounded lifetime. The full month and existing/manual city labels are available offline. Localized common names use a canonical Latin fallback when a native form is unavailable. See [2.7 behavior and remaining device checks](docs/UPDATE_2_7_FA.md).
