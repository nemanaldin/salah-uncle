# Offline training-video metadata

The existing empty `assets/training/manifest.json` is unchanged. Future entries have stable `id` and `assetPath`, explicit `school`, `languages` (BCP-47 tags), `reviewStatus` (`reviewed` or `experimental`), localized `titles`, `source` and `version`. English is the title fallback. Scoped entries require an English title, version and HTTPS source; old unscoped entries remain parseable but are not presented as belonging to any school.

The repository retains all source languages/schools. The UI filters by the central active policy and the exact selected school/language. An experimental language cannot become reviewed because a video is labelled reviewed. No movie, audio file, religious wording or purported source was added. Playback and future Google Flow media still require device QA.
