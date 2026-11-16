# Source removal — 2026-11-16

The owner requested complete removal of the previous primary source. No replacement attribution was invented. The source scan initially failed on the existing app data, then passed after removal.

Changed locations:

- `app/src/main/assets/content/hanafi-fa-v1.json`: the primary source URL for all 21 legacy lesson IDs is empty and `sourceReviewRequired=true`. All 73 original Persian step bodies, IDs, audio references and illustrations are unchanged.
- `app/src/main/assets/content/sunni-guides-v1.json`: primary sources of `prayer-hanafi` and `purification-hanafi` are empty with review flags. Their source lists in all ten language variants are empty. The sentence promising references below was removed from the first section of each of those twenty variants; the surrounding rulings and recitations are unchanged. Other schools are not borrowed as replacement references.
- `app/src/main/assets/translations/legacy-{en,ar,tr,ur,id,bn,ms,ps,fr}-v1.json`: regenerated source digest headers after the primary-source metadata changed. Translation bodies are byte-for-byte equivalent strings; no prayer text was rewritten.
- `docs/CONTENT_SOURCES.md`: the endorsed-source row was replaced with an explicit source-review requirement.
- `LessonRepository.kt`, `LessonScreen.kt` and `SalahApp.kt`: parse the missing-source review flag, show its localized notice, and do not show a clickable empty source URL. Remaining genuine references are retained.
- `ui_strings.xml` in all ten locales: one operational source-review notice was added.
- `scripts/verify_sources.py`: scans all main app text and content templates for the rejected source and checks missing-provenance status; `verify_content.py` runs it while retaining catalog, body/media hashes and translation-isolation checks.
- `ContentCatalogTest.kt`: its source-presence assertion now accepts an explicit missing-source review flag. No test was deleted or disabled; other original tests and all audio remain unchanged.

`source-removal-changes.json` records all 63 data edits (23 source fields, 20 per-language source lists, 20 incomplete reference promises). Removed URLs/titles are represented only by counts/digests; the log is not a new reference source. Existing Git history and published historical artifacts were not rewritten. There was no push.

Checks: build, 190 unit tests, lint 65 warnings/0 errors, Android test compilation, ten-locale content verification and the removed-source scan passed. Android execution remains unverified: no adb target and no /dev/kvm are available.
