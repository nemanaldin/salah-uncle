# Selectable adhan recordings

Settings → Adhan voice lists the existing recording and files the user chooses through Android's document picker. Each recording can be previewed, selected as the default, or removed. Selection and metadata are stored in the existing DataStore. A selected document is copied to the app's private directory, so document-provider access, file moves, or loss of internet do not interrupt later alarms.

Imports are limited to 20 MiB per recording, copied in bounded chunks on an IO dispatcher, and checked for an actual audio track and nonzero duration with Android MediaMetadataRetriever. Failed/cancelled imports remove the incomplete file and retain the prior selection. A failed settings write also removes the newly copied file. Selecting a school or regional calculation method preserves the recordings. Removing the selected recording restores the existing default and deletes its private copy.

The alarm coordinator still calculates and schedules exactly as before. It reads the latest selected recording **at delivery time**, so changing voice does not reschedule, cancel, or shift a prayer alarm. The service receives only a validated private filename, retains its alarm audio attributes, foreground notification, wake lock, audio focus handling and stop action, and falls back to the existing recording if a selected file has disappeared. Preview playback remains separate from the actual adhan service, stops on screen exit/backgrounding and releases its player.

## Files and rights

No new bundled recording has been added. The existing `dhan_sound` is **1,252,613 bytes / 156.53 seconds**, unchanged from the owner's original repository. Its original recording attribution/reuse permission is not documented in that repository, so continued presence does not constitute a new licence clearance. This should be resolved with the owner before public distribution. No copy of it is represented as newly sourced audio.

Sources inspected for possible additions:

- [Adhan.ogg — Aishatu98](https://commons.wikimedia.org/wiki/File:Adhan.ogg), source page says own work / CC0; about 42 seconds / 331 KB.
- [Beautiful adhan.ogg — Adam-synagda](https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg), source page says own work / CC0; about 154 seconds / 1.17 MB.

These source-page descriptions do not establish full recording quality or suitability. The environment's network policy does not allow downloading their media hosts, so the files were not obtained, listened to, or bundled. No fake/generated recording or unverified conversion is substituted.

To add named built-in choices later, supply the actual audio file, reciter/recordist, permanent source URL, exact licence or written permission, any attribution text, and whether it is a full adhan or excerpt. The import picker already allows multiple recordings supplied by the owner/user with appropriate permission. Imported files increase app storage, **not the APK download**. Uninstalling the app removes these private copies.

## Validation scope

Unit tests exercise unsafe filename rejection, missing selection fallback and preservation of voice/alerts when changing school. The Android test provider exists only in the test APK and reuses the unchanged original recording to test real document import, metadata extraction, selection, preview and removal. The production manifest contains no provider for exposing private recordings. Screen-off/background playback, reboot, physical-device battery restrictions and audio interruptions still require device QA.
