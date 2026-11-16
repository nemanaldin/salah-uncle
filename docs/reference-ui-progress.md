# Reference UI follow-up — 2026-11-16

Branch: `work`; baseline: `8dc4ad65648dd37548b58b64723dcc47abd0e2a2`. The current owner message includes the previously missing reference image and invokes `claude prompt.txt`. No push, new branch, history rewrite or PR mutation is authorized.

The existing dua catalog, source removal, offline OFL fonts, optional reminders and pending content-review flags were inspected and retained. The image now unblocks the general UI item from `duas-progress.md`; the old PDF remains a historical record.

| Item | Status | Commit | Evidence / limits |
|---|---|---|---|
| Inspect branch and baseline | done | 8dc4ad6 | clean; baseline Build, 218 unit tests, lint 65 warnings/0 errors and Android-test compilation passed |
| Match home photo/card hierarchy, six shortcuts and palette | done | 1a4b4db | reuse the exact existing art shown in the image; current build/unit/lint pass; real times/date/city remain data-driven |
| Today/Learn/Tools/Dua navigation; settings via header | done | 1a4b4db | direct dua tab and line icons; settings, search, saved topics, slides and future-video route retained |
| Readable countdown and responsive RTL/LTR layout | done | 1a4b4db | 8 added unit tests including adaptive layout; phone-sized normal layout is two columns; large fonts/narrow width use one |
| Preserve newest owner Arabic formatting | done | 1a4b4db | only the second owner's seven separators become line breaks, matching the newly attached file exactly; word sequence and review flags unchanged |
| Recheck source/font/catalog/translation integrity | done | 1a4b4db | 70 topics, 11 empty Arabic fields and all pending flags retained; original lesson/translation/audio bytes preserved |
| Build, all unit tests, lint and Android-test compilation | done | 1a4b4db | final reference-ui-reviewed-final: 226/226 unit tests; 65 warnings/0 errors; build and compile passed |
| Native Android visual/flow checks | blocked | 1a4b4db | pre-review APK and test APK installed (delivered APK only built); four tests selected but none completed; guest Android system ANR, no KVM, manual capture timed out; no app screenshot validated; existing AVD/user data untouched |
| Printed-source comparison | blocked, inherited | d8a69c2 | no new religious wording or scholarly approval; previous documented print-download limits remain |
| Updated downloadable APK/report/source | done | 1a4b4db | local APK, PDF, ZIP and Git bundle; final APK signer matches previous local d8a69c2 APK; native and printed review blocked |

Reference takes precedence over the previous layout: Settings moves from the fourth bottom tab to the header; Dua takes its tab; six pictured shortcuts replace the larger home grid; reading resume moves to Learn. Additional topics remain in Learn, and Tools retains prayer times, Qibla, Ramadan and the future Google Flow page. The full prayer-times page keeps its numeric seconds countdown.

## Owner-authorized publication follow-up — 2026-11-16

The owner subsequently requested updating GitHub and a downloadable PDF. Repository metadata confirms `nemanaldin/salah-uncle` is the authenticated owner's repository; the earlier transposed address returned 404. Remote `work` was `616535b12d17f8adda2d62767070156459ad46b3` and is an ancestor of the local history. Publication targets only `refs/heads/work`, with no force, main/other-branch change or PR #1 mutation.

The exact 13-page PDF from the preceding local delivery is now included under `docs/reports/2026-11-16/`, with its checksum and a direct-download index. The prior no-push statements are historical; this follow-up records the later explicit authorization. Publication verification is separate from application tests; no app code or content changes are part of this step.

Publication verified: commit `285c94590a3c5bb644164ee321d08c58f06203e0` was pushed as a fast-forward to the existing `work` branch. The public PDF download was fetched back and matched the local 424,421-byte, 13-page report exactly (SHA-256 `2670a171f264a1c6820c6162cf9d4b71178854dc21fdee8f751216de58a6c93d`). Remote `main`, `uncle-work` and `kotlin-compose` hashes were checked unchanged; PR #1 was not mutated. The accompanying publication-verification JSON records this check.

## Public APK download follow-up — 2026-11-16

The owner reported that the workspace-only APK link could not be downloaded or shared. The exact existing APK from code commit `1a4b4db` is now included under `downloads/Salah-2.7.0-reference-ui-debug.apk`, with a Persian download guide and checksum. This preserves its existing debug signing certificate and avoids delivering a newly signed CI build in its place. No app code, fiqh wording, branch policy, PR or existing releases are changed. Direct download is verified independently after pushing.

Public APK delivery completed: commit `a2bdf54b7a18043d97f99c5fbc2bc4a07f1dc1e1` was pushed to the same `work` branch. The public URL was downloaded without supplying GitHub credentials and its 31,948,450 bytes matched the existing local APK SHA-256 `24330c7bac1ea1ffa8873400abd58f579f459148cea03af2b7ad2cbd8f7b43c7`. All other branch heads and app sources were checked unchanged. `downloads/reference-ui-download-verification.json` records the successful download check.
