# Reference UI follow-up — 2026-11-16

Branch: `work`; baseline: `8dc4ad65648dd37548b58b64723dcc47abd0e2a2`. The current owner message includes the previously missing reference image and invokes `claude prompt.txt`. No push, new branch, history rewrite or PR mutation is authorized.

The existing dua catalog, source removal, offline OFL fonts, optional reminders and pending content-review flags were inspected and retained. The image now unblocks the general UI item from `duas-progress.md`; the old PDF remains a historical record.

| Item | Status | Commit | Evidence / limits |
|---|---|---|---|
| Inspect branch and baseline | done | 8dc4ad6 | clean; baseline Build, 218 unit tests, lint 65 warnings/0 errors and Android-test compilation passed |
| Match home photo/card hierarchy, six shortcuts and palette | done | next local commit | reuse the exact existing art shown in the image; current build/unit/lint pass; real times/date/city remain data-driven |
| Today/Learn/Tools/Dua navigation; settings via header | done | next local commit | direct dua tab and line icons; settings, search, saved topics, slides and future-video route retained |
| Readable countdown and responsive RTL/LTR layout | done | next local commit | 8 added unit tests including adaptive layout; phone-sized normal layout is two columns; large fonts/narrow width use one |
| Preserve newest owner Arabic formatting | done | next local commit | only the second owner's seven separators become line breaks, matching the newly attached file exactly; word sequence and review flags unchanged |
| Recheck source/font/catalog/translation integrity | done | next local commit | 70 topics, 11 empty Arabic fields and all pending flags retained; original lesson/translation/audio bytes preserved |
| Build, all unit tests, lint and Android-test compilation | done | next local commit | final reference-ui-reviewed-final: 226/226 unit tests; 65 warnings/0 errors; build and compile passed |
| Native Android visual/flow checks | blocked | next local commit | APK and test APK installed; four tests selected but none completed; guest Android system ANR, no KVM, manual capture timed out; no app screenshot validated; existing AVD/user data untouched |
| Printed-source comparison | blocked, inherited | d8a69c2 | no new religious wording or scholarly approval; previous documented print-download limits remain |
| Updated downloadable APK/report/source | in progress | — | local outputs only; signing compatibility will be checked |

Reference takes precedence over the previous layout: Settings moves from the fourth bottom tab to the header; Dua takes its tab; six pictured shortcuts replace the larger home grid; reading resume moves to Learn. Additional topics remain in Learn, and Tools retains prayer times, Qibla, Ramadan and the future Google Flow page. The full prayer-times page keeps its numeric seconds countdown.
