# Duas and reading-font work — 2026-11-16

Current branch: work. Start: 616535b12d17f8adda2d62767070156459ad46b3. Current request explicitly forbids push; no remote changes, main/PR #1 mutation or branch creation are authorized. Preserve existing slides/search/bookmarks/locales. Removal of the rejected source and verified Hanafi dua additions are explicitly authorized by this new request; earlier immutable-content guidance does not block those narrowly scoped edits.

The new message has no image attachment. The owner supplied the missing categories 3–7 and all three exact Arabic texts asynchronously; they are now authorized scope, including default-off optional reminders. General visual redesign is blocked until the reference image is supplied; no colors/layout will be invented for it. New dua cards may use existing app components as requested.

| Item | Status | Commit | Evidence / limitation |
|---|---|---|---|
| Baseline and current-state inspection | done | 616535b | Build, 190 unit tests, lint 65 warnings/0 errors, Android test compilation and content check passed |
| Remove the rejected source from app data, sources, comments and translations | done | 7e4084a21dfcc3775a5512638e67f784d6d7370c | 63 data edits; source regression initially failed then passed; 190 tests; lint 65/0; 73 original legacy bodies preserved |
| Bundle 3–5 OFL fonts and persistent reading-font controls | done | 3f147d1 | 5 OFL families, 1,254,020 added font bytes; 197 tests; lint 65/0; preview/persistence instrumentation compiled only |
| Hanafi-only dua data with explicit source/grading/review status | partly; content review pending | 6ab373c, d8a69c2 | final: 70 topics; 52 source-compared dua texts + 4 guidance evidences + 3 exact owner texts; 11 empty Arabic fields; all pronunciation/new translations pending review |
| Dua cards, subcategories, search, bookmarks/recent and scoped pager | done; device unverified | 233f795 | 218 tests; build, lint 65/0 and Android-test compilation passed; source/copy/audio/guide paths integrated |
| Default-off prayer-reminder scheduling, permission, reboot and weighted no-repeat deck | done; phone delivery unverified | 233f795 | independent inexact alarm/channel/receivers; consent/permission/deck/DST tests passed; OS delivery and reboot unverified |
| Post-implementation printed-source comparison of pending fields | blocked; online comparison partly done | d8a69c2 | 70 actual field inventories and Hanafi review report; PDF too large/403/timeout; precise Wasilat appendix not read; no print approval claimed |
| General UI matching reference image | blocked | — | no reference image was received |
| Source refinements and final diff review | done | d8a69c2 | 70 topics; 52 source-compared dua texts, 4 guidance evidences, 3 owner texts; 11 empty Arabic fields; final build/test/lint: 218/218, 65 warnings/0 errors; instrumentation compiled only |
| Final review/build/unit/lint and portable local outputs | done; device and printed review remain blocked | d8a69c2 | 218 tests; lint 65/0; signed debug APK, 13-page PDF, source ZIP and Git bundle; no push; signer differs from previous published APK |
