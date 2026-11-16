# Duas and reading-font work — 2026-11-16

Current branch: work. Start: 616535b12d17f8adda2d62767070156459ad46b3. Current request explicitly forbids push; no remote changes, main/PR #1 mutation or branch creation are authorized. Preserve existing slides/search/bookmarks/locales. Removal of the rejected source and verified Hanafi dua additions are explicitly authorized by this new request; earlier immutable-content guidance does not block those narrowly scoped edits.

The new message has no image attachment and only specifies categories 1–2. Categories 3–7 and the three owner-supplied texts mentioned for category 7 were requested asynchronously. General visual redesign is blocked until the reference image is supplied; no colors/layout will be invented for it. New dua cards may use existing app components as requested.

| Item | Status | Commit | Evidence / limitation |
|---|---|---|---|
| Baseline and current-state inspection | done | 616535b | Build, 190 unit tests, lint 65 warnings/0 errors, Android test compilation and content check passed |
| Remove the rejected source from app data, sources, comments and translations | done | this commit | 63 data edits; source regression initially failed then passed; 190 tests; lint 65/0; 73 original legacy bodies preserved |
| Bundle 3–5 OFL fonts and persistent reading-font controls | not started | — | default must remain current Vazirmatn; font files/licenses require verification |
| Hanafi-only dua data with explicit source/grading/review status | in progress | — | direct-source research; unknown Arabic/source fields remain empty |
| Dua cards, subcategories, search, bookmarks/recent and scoped pager | not started | — | missing categories 3–7 must not be guessed |
| General UI matching reference image | blocked | — | no reference image was received |
| Final review/build/unit/lint and portable local outputs | not started | — | no push |
