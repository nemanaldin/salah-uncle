# Duas and reading-font work — 2026-11-16

Current branch: work. Start: 616535b12d17f8adda2d62767070156459ad46b3. Current request explicitly forbids push; no remote changes, main/PR #1 mutation or branch creation are authorized. Preserve existing slides/search/bookmarks/locales. Removal of the rejected source and verified Hanafi dua additions are explicitly authorized by this new request; earlier immutable-content guidance does not block those narrowly scoped edits.

The new message has no image attachment. The owner supplied the missing categories 3–7 and all three exact Arabic texts asynchronously; they are now authorized scope, including default-off optional reminders. General visual redesign is blocked until the reference image is supplied; no colors/layout will be invented for it. New dua cards may use existing app components as requested.

| Item | Status | Commit | Evidence / limitation |
|---|---|---|---|
| Baseline and current-state inspection | done | 616535b | Build, 190 unit tests, lint 65 warnings/0 errors, Android test compilation and content check passed |
| Remove the rejected source from app data, sources, comments and translations | done | 7e4084a21dfcc3775a5512638e67f784d6d7370c | 63 data edits; source regression initially failed then passed; 190 tests; lint 65/0; 73 original legacy bodies preserved |
| Bundle 3–5 OFL fonts and persistent reading-font controls | done | next local commit | 5 OFL families, 1,254,020 added font bytes; 197 tests; lint 65/0; preview/persistence instrumentation compiled only |
| Hanafi-only dua data with explicit source/grading/review status | in progress | — | direct-source research; unknown Arabic/source fields remain empty |
| Dua cards, subcategories, search, bookmarks/recent and scoped pager | not started | — | all seven categories received; reuse existing app components while reference-image redesign stays blocked |
| Default-off prayer-reminder scheduling, permission, reboot and weighted no-repeat deck | not started | — | inexact alarms; never share timing logic or request codes with adhan |
| Post-implementation printed-source comparison of pending fields | not started | — | owner suggested al-Adhkar and Yahya al-Mulla appendix to Wasilat al-Falah; verify actual editions/pages without fabricated citations |
| General UI matching reference image | blocked | — | no reference image was received |
| Final review/build/unit/lint and portable local outputs | not started | — | no push |
