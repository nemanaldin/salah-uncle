# Audit continuation progress

Working branch: `work` (unchanged). Existing 2.7 history was fast-forwarded into this branch without a merge commit.

Verified baseline: `a09fbbd23cf3f1026e09ba0be56d8079e9a86849` (2.7.0/code 14), not the initial environment checkout `6dc5366` (2.2). No earlier audit-progress file or fixes were present.

Canonical origin: `nemanaldin/salah-uncle`; requested `uncle-salah/nemanaldin` returned 404. No remote `work` branch exists, so commits remain local: creating a remote branch or updating the PR branch would violate the branch/PR constraints. No push, main update or PR #1 mutation.

All instructional content and translations must remain byte-for-byte unchanged. New operational UI strings are allowed; no religious wording is approved by readiness labels.

| Item | Description | Status | Commit | Tests / limits |
|---|---|---|---|---|
| 1.1 | Late adhan notifications | done | 10e824234ecdfef2e9d2e5c05279babd923f5c63 | 8 delay-boundary tests passed; legacy policy reproduced 4 failures before fix; device delivery unverified |
| 1.2 | Qibla sensor states | done | f0c5f890fe62ba36f758c26baa15b382e2d9710c | 9 heading-state tests passed; physical sensors and registration failure on device unverified |
| 1.3 | Home and settings recovery states | done | c2a351757a1886a054bc56f26e52033cfcf3ec06 | 10 home-state and read-recovery tests passed; no device I/O fault injection |
| 1.4 | First-launch language selection | done | 7ef02aa9d8a694eec5ba9f735393f28c571f627c | 7 language initialization tests passed; Android locale recreation requires phone check |
| 1.5 | Independent content loading | done | 5a17834efe8b31045b94bbd1800a0fa772b0ae13 | 4 independent loading tests passed; reader, picker and video placeholder use separate errors |
| 1.6 | Lazy lesson audio | done | ab582d2c5c0a62411c62e3774406d8eda87801dc | 4 lazy resource lifecycle tests passed; real audio and audio focus unverified |
| 2.1 | Multi-source location fallback | done | 4df7c54b1e06fe228748f8df3ea033cdd93c0c8b | 9 location selection tests passed; preferred 2min/200m, fallback 10min/5km; GPS and permission behavior on phone unverified |
| 2.2 | Calculation method confirmation | done | 0e5bb73d36c679401f8cca3d5c291be16a8e5668 | 6 confirmation tests passed; Karachi retained; confirmation persists with coordinates zone method and Asr |
| 2.3 | Umm al-Qura Ramadan Isha | done | 7e80177c91638f5f51baa3d4fd1cbe5b78f42ca6 | 7 tests passed; first and last Ramadan days reproduced 90 vs 120 failures before fix; Saudi calendar assumption documented |
| 2.4 | Partial high-latitude prayer times | done | this item commit (resolved immediately after creation) | 10 new partial-day tests passed plus Ramadan regression tests; Tromso crash reproduced before fix; no invented times |
| 3 | Language and variant readiness | not started | — | — |
| 4 | School-specific content review report | not started | — | — |

## Actual check results

| Check | Unit tests | Lint warnings / errors | Result |
|---|---|---|---|
| baseline | 52/52 | 65 / 0 | build + unit + lint passed |
| phase1 | 94/94 | 66 / 0 | build + unit + lint passed |

Phase 1 lint first hit an internal JVM MethodHandle error with a temporary C1-only JVM option. Restarting the daemon with normal Java 21 options completed lint; project tool versions were unchanged.


Baseline and phase checks: `/workspace/audit-evidence/` (actual logs/results). No local adb target or /dev/kvm; device, sensors, GPS and audio execution are not verified.
