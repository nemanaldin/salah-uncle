#!/usr/bin/env bash
set -u
salah_test_status=0
./gradlew --no-daemon --console=plain --max-workers=3 :app:connectedDebugAndroidTest || salah_test_status=$?
mkdir -p app/build/qa-screenshots
adb pull /sdcard/Android/data/com.androidhive.myapplication/files/qa-screenshots app/build/qa-screenshots || true
exit "$salah_test_status"
