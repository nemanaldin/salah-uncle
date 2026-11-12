# Audit continuation progress

Working branch: `work` (unchanged). Existing 2.7 history was fast-forwarded into this branch without a merge commit.

Verified baseline: `a09fbbd23cf3f1026e09ba0be56d8079e9a86849` (2.7.0/code 14), not the initial environment checkout `6dc5366` (2.2). No earlier audit-progress file or fixes were present.

Canonical origin: `nemanaldin/salah-uncle`; requested `uncle-salah/nemanaldin` returned 404. No remote `work` branch exists, so commits remain local: creating a remote branch or updating the PR branch would violate the branch/PR constraints. No push, main update or PR #1 mutation.

All instructional content and translations must remain byte-for-byte unchanged. New operational UI strings are allowed; no religious wording is approved by readiness labels.

| Item | Description | Status | Commit | Tests / limits |
|---|---|---|---|---|
| 1.1 | Late adhan notifications | done | 10e824234ecdfef2e9d2e5c05279babd923f5c63 | 8 delay-boundary tests passed; legacy policy reproduced 4 failures before fix; device delivery unverified |
| 1.2 | Qibla sensor states | done | f0c5f890fe62ba36f758c26baa15b382e2d9710c | 12 heading-state tests passed including invalid-matrix recovery; follow-up 50245b89a9cd6b356b663bf9f033a622aa1b390a; physical sensors unverified |
| 1.3 | Home and settings recovery states | done | c2a351757a1886a054bc56f26e52033cfcf3ec06 | 11 home/read tests passed including a reproduced cancellation race; follow-up b8430daba1c39357f763667f81727e403d680a3b; no device I/O fault injection |
| 1.4 | First-launch language selection | done | 7ef02aa9d8a694eec5ba9f735393f28c571f627c | 7 language initialization tests passed; Android locale recreation requires phone check |
| 1.5 | Independent content loading | done | 5a17834efe8b31045b94bbd1800a0fa772b0ae13 | 5 independent loading tests passed; non-cooperative failure regression added in b8430daba1c39357f763667f81727e403d680a3b |
| 1.6 | Lazy lesson audio | done | ab582d2c5c0a62411c62e3774406d8eda87801dc | 4 lazy resource lifecycle tests passed; real audio and audio focus unverified |
| 2.1 | Multi-source location fallback | done | 4df7c54b1e06fe228748f8df3ea033cdd93c0c8b | 9 location selection tests passed; preferred 2min/200m, fallback 10min/5km; GPS and permission behavior on phone unverified |
| 2.2 | Calculation method confirmation | done | 0e5bb73d36c679401f8cca3d5c291be16a8e5668 | 6 confirmation tests passed; Karachi retained; confirmation persists with coordinates zone method and Asr |
| 2.3 | Umm al-Qura Ramadan Isha | done | 7e80177c91638f5f51baa3d4fd1cbe5b78f42ca6 | 7 tests passed; first and last Ramadan days reproduced 90 vs 120 failures before fix; Saudi calendar assumption documented |
| 2.4 | Partial high-latitude prayer times | done | 47f70e085bc49b8b90b88e1c65beb8a3af33acc4 | 10 new partial-day tests passed plus Ramadan regression tests; Tromso crash reproduced before fix; no invented times |
| 3 | Language and variant readiness | done | 136cc2e630c0aeb4615bd9b6b1b010cb3eee23fc | 5 metadata unit tests passed; 330 variants checked; 1 Android repository test added and compiled but not run; all content hashes preserved |
| 4 | School-specific content review report | done | 63117f868af9f17aa0a14da75ee40716ef56b7ec | 11 report entries with standard-book chapters, draft wording and confidence; no religious correctness test or scholar approval claimed |

## Actual check results

| Check | Unit tests | Lint warnings / errors | Result |
|---|---|---|---|
| baseline | 52/52 | 65 / 0 | build + unit + lint passed |
| phase1 | 94/94 | 66 / 0 | build + unit + lint passed |
| phase2 | 126/126 | 66 / 0 | build + unit + lint passed |
| phase3 | 131/131 | 66 / 0 | build + unit + lint passed |
| phase4 | 131/131 | 66 / 0 | build + unit + lint passed |
| final | 136/136 | 66 / 0 | build + unit + lint passed |

Phase 1 lint first hit an internal JVM MethodHandle error with a temporary C1-only JVM option. Restarting the daemon with normal Java 21 options completed lint; project tool versions were unchanged.


## Final diff review

Done: full 2,726-line diff and subsequent fixes read. Cancelled-read race fixed in b8430daba1c39357f763667f81727e403d680a3b; invalid sensor matrix follow-up 50245b89a9cd6b356b663bf9f033a622aa1b390a. Five review regression tests added. Final build, all 136 unit tests, lint (66 warnings/0 errors), Android test compilation and content verification passed. No local adb target; device tests remain unverified.


Baseline and phase checks: `/workspace/audit-evidence/` (actual logs/results). No local adb target or /dev/kvm; device, sensors, GPS and audio execution are not verified.

## Publication follow-up — 2026-11-09

The owner subsequently requested a GitHub push and an installable APK download. Commit `177d3abc772c82cbcde905109e0213c9c25c0ab7` was pushed successfully to the same fork's `work` branch. Remote `main` and `uncle-work` remained at their earlier hashes; PR #1 was not edited.

A separate `Download audit APK` workflow builds and tests this branch and publishes a debug APK as a clearly labelled prerelease with a SHA-256 checksum. Its first run is pending at the time of this commit. The local APK and the preceding audit's test evidence remain available; no app source or educational wording changed in this publication follow-up. The earlier no-push statements describe the original audit hand-off before this new authorization.

Publication completed: [workflow run](https://github.com/nemanaldin/salah-uncle/actions/runs/42687777533) succeeded, including unit tests, APK assembly and prerelease upload. [Download the test APK](https://github.com/nemanaldin/salah-uncle/releases/download/audit-apk-8b1f79a2a08b/Salah-2.7.0-audit-debug.apk) from the [release](https://github.com/nemanaldin/salah-uncle/releases/tag/audit-apk-8b1f79a2a08b). The published APK was downloaded again, its SHA-256 was verified (`91a922eaef4d799d5bd26ef9245a4d22f5641565c52ebcccede3520a599d7770`), and Android package metadata confirmed version 2.7.0/code 14, minSdk 21 and targetSdk 36. APK source commit: `8b1f79a2a08b32f29327628c45def88e85afecca`; subsequent publication documentation does not change app code. Physical-device checks remain pending.

## Continuation — 2026-11-12

Authorized by salah-app-prompt.docx: current branch work; local commits only, no push, no PR/main changes. Start 1bfe7a91749d1e513d39d7e18204dd7fef823190. Baseline: build, 136 unit tests, lint 66 warnings/0 errors passed. Existing instructional assets, audio and tests are preserved.

| Phase | Work | Status | Commit | Evidence / limits |
|---|---|---|---|---|
| 5 | Active languages/school and safe settings recovery | done | e5a1d130572bc4a8539514591d86b052c20943dc | Build, 142 unit tests, lint 67 warnings/0 errors; existing assets/audio/tests preserved; font cmap checked; device shaping unverified |
| 6 | Article contents, scoped full-text search and section bookmarks | done | 929e41287ae6cecd67525ded9176b4b1a62ff0b0 | Build, 149 unit tests, lint 67 warnings/0 errors; 7 new navigation/bookmark tests; no device persistence/old UI-suite certification |
| 7 | School/language-scoped video metadata | done | 9909b0b0284492e9800ccd533469e6379dfa1ad8 | Build, 155 unit tests, lint 67 warnings/0 errors; 6 video isolation/fallback tests; no video files added or playback certified |
| 8 | Optional prayer/wudu/ghusl slides and text view | done | 3fc5c823df584123b52ff8b1e4717908566c76ff | Build, 161 unit tests, lint 65 warnings/0 errors; 6 exact-text/navigation tests; actual swipes unverified |
| 9 | Draft dhikr arrangement and post-wudu integration | done | ee4a0436063941d26ead6a2e1a0bed5d93c0d57d | Build, 168 unit tests, lint 65 warnings/0 errors; 7 source/order/school/migration tests; all source wording unchanged; scholarly/device review pending |
| 10 | Accessibility and large-text/dark/RTL quality | done | 4c56a290599df515b8047e1850915cdf70a04531 | Build, 170 unit tests, lint 65 warnings/0 errors; Android tests compiled; connected test failed: no connected devices; TalkBack/visual shaping unverified |
| 11 | Compact adaptive quick access | done | 796e34af1e26e412a673400f3b2a66fe16cf82be | Build, 173 unit tests, lint 65 warnings/0 errors; 3 new grid policy tests and compiled click test; previews compile, device layout unverified |
| 12 | Existing tayammum artwork and school-isolated entry | done | c8454fb90b9b6eab594fe918d8331bdd1622def3 | Build, 177 unit tests, lint 65 warnings/0 errors; 4 exact scope tests; original sheet intact, zero new image bytes; device rendering unverified |
| 13 | Consistent draft step artwork | done | 204e0b1f7645397b6dd6220e9ebb349a7f062c6c | Build, 184 unit tests, lint 65 warnings/0 errors; 7 art mapping/alt/scope tests; 30 stages illustrated, 156174 WebP bytes; poses/gender variants pending owner review |

### Continuation final review

Final corrections: 5f8285f575346279b87beae2dc26573db604f54f. The complete continuation diff and subsequent fixes were re-read. An additive preference migration now preserves raw unrelated settings and records the prior school/language scope before visibility recovery. Reading-entry resume targets remain stable during progress updates. Long notices scroll inside guide pages and variant readiness is honored there. Missing overview dhikr sources and blank video titles are handled explicitly.

Final checks: assembleDebug, 190/190 unit tests (54 more than baseline), lint 65 warnings/0 errors, Android test compilation and content validation passed. Original 92 protected files unchanged. No phone/emulator is connected; connectedDebugAndroidTest failed with No connected devices. Phase implementation statuses above are done; phase 10 device QA and phase 13 owner/scholarly artwork approval remain blocked/pending, as reflected by partly in audit-fixes-review-2.md. Existing legacy UI assumptions were preserved unchanged and are not certified compatible. No push, main or PR #1 mutation was performed.

### Publication follow-up — 2026-11-12

After the local handoff, the owner explicitly requested a downloadable PDF link and publication of the changes to GitHub. This new authorization supersedes the earlier no-push instruction for this publication only. Destination remains `nemanaldin/salah-uncle`, existing branch `work`; main, uncle-work and PR #1 are excluded. Preflight observed work at 1bfe7a91749d1e513d39d7e18204dd7fef823190, main at 6dc5366532cefbb295b14237e10b7d3d2dc0f859, uncle-work at a09fbbd23cf3f1026e09ba0be56d8079e9a86849. The report source is 2d618418c7158a7a268bec9339bd43f25fe6bce9. The exact PDF and its checksum are added under docs/reports/2026-11-12; app code and prior audit text are unchanged. Status: publication in progress; completion is verified from the remote after push.

Publication completed: fast-forward push from 1bfe7a91749d1e513d39d7e18204dd7fef823190 to 47dc2f9d74939d69aff0d813bd75f090cb48e946 succeeded. The GitHub connector independently confirmed the work head. The public [PDF download](https://github.com/nemanaldin/salah-uncle/raw/refs/heads/work/docs/reports/2026-11-12/Salah-Continuation-Review.pdf) was downloaded again and checked: 9 pages, 1,433,758 bytes, SHA-256 07134d67755b9365a233f7dbaa8cc1ddae12899900b6d3cb861abc7d71703f8b, identical to the original report. Remote main and uncle-work still have the exact preflight hashes above; PR #1 was not mutated. App source and its last 190-test local verification are unchanged by the PDF/documentation publication. This receipt is committed and sent to the same work branch as a documentation-only follow-up.

### Current APK download publication — 2026-11-12

The owner requested a downloadable APK for the current changes. The local current APK remains available (SHA-256 b7d573511e43c99d40553cf235feb2cf18f502d137f806b40891653b95a04e05; 31,152,738 bytes). The previously published GitHub APK targets 8b1f79a2a08b and does not include continuation phases 5–13. The existing work-branch download workflow is updated to build/test the current source and publish a separate, explicitly labelled continuation prerelease with the APK, unchanged PDF and checksums. Prior releases are preserved. App code is unchanged by this publication; device execution remains unverified. Status: publication in progress; final remote assets and their checksum will be verified before completion.

Current APK publication completed: workflow [43248726510](https://github.com/nemanaldin/salah-uncle/actions/runs/43248726510) succeeded for source 640f4e211fc679fdc9b7424876b5a1b9db765be5, including content validation, testDebugUnitTest, assembleDebug, artifact upload and prerelease creation. [Current test release](https://github.com/nemanaldin/salah-uncle/releases/tag/continuation-apk-640f4e211fc6) preserves the older audit release. [Download the current APK](https://github.com/nemanaldin/salah-uncle/releases/download/continuation-apk-640f4e211fc6/Salah-2.7.0-continuation-debug.apk). The APK and PDF were downloaded again and both match the release checksums. APK: 30,383,979 bytes; SHA-256 3311327d73530001046d83ccdc6d7c70320c0fd26f7b0743f4b08185fa5ad9d7; package com.androidhive.myapplication, version 2.7.0/code 14, minSdk 21/targetSdk 36. apksigner verified its signature; all bundled assets and both new art files match source. App tree f53221b3b6b2b118373765e9d3ccd09684c55c5e matches the earlier 190-test local handoff.

The current GitHub debug certificate differs from the prior published GitHub APK and from the local APK. An in-place upgrade from that older signed APK will be rejected by Android. Do not uninstall an existing installation with saved bookmarks/settings just to install this test; a compatible signing key or a separately planned data-preserving transition is required. Phone execution remains unverified. Main, uncle-work and PR #1 are unchanged; this completion receipt only changes documentation on work and does not trigger another APK build.
