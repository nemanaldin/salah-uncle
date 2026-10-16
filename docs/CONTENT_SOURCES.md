# Four-school prayer introductions

The 2.4.0 addition covers Hanafi, Shafii, Maliki and Hanbali, following the requested scope. Jafari is not offered. The existing screen layout, colors, navigation and Google Flow placeholder remain.

Each school has a separate prayer lesson with 10 steps and a wudu lesson with 4 steps. Both are available offline in Persian, English, Arabic, Turkish, Urdu, Indonesian, Bengali, Malay, Pashto and French. Lesson IDs are separate per school; step IDs remain stable across translations, so changing language preserves progress without borrowing another school's lesson.

These are introductory instructions for individual daily prayer. They do not attempt to cover all rulings on congregational prayer, travel, sickness, Friday prayer or witr. Recitations are Arabic; explanatory prose and titles are localized. The existing Hanafi audio is reused only for shared Arabic takbir, ruku and sujud formulas. No new animation or voice recording was generated.

The previous 21 Hanafi lessons and their 73 Persian steps, audio associations and illustrations are preserved exactly. Their titles are translated into the ten languages; the longer original bodies remain explicitly tagged Persian. The reader identifies this language when it differs from the interface language.

## Sources and scope of adaptation

The new guides use original instructional wording, with source links visible within the relevant lesson and on its school's Sources page. They are not reproductions of entire source articles. Arabic Quranic passages and traditional prayer formulas retain their recitation language.

| School | References consulted | Distinctions represented |
| --- | --- | --- |
| Hanafi | [Original Erfanabad material supplied with the app](http://www.erfanabad.org/fa/modules/news/article.php?storyid=41), preserved in `hanafi-fa-v1.json` | Quiet basmala and Amin, no regular daily Fajr qunut, Hanafi Asr convention; posture details refer to the original material |
| Shafii | [Umm Umar Bozai / Mostafa Azzam, based on al-Risalat al-Jamiah, Qibla archive](https://islamqa.org/shafii/qibla-shafii/34020/); [SeekersGuidance on Fajr qunut](https://seekersguidance.org/answers/shafii-fiqh/what-is-the-ruling-on-the-qunut-for-the-fajr-prayer/) | Basmala as part of Fatiha, hand raising around ruku, recommended second-unit Fajr qunut after ruku |
| Maliki | [Ibn Abi Zayd al-Qayrawani, al-Risala: prayer, hosted at IIUM](https://www.iium.edu.my/deed/lawbase/risalah_maliki/book10.html); [wudu chapter](https://www.iium.edu.my/deed/lawbase/risalah_maliki/book04.html); [Abdullah bin Hamid Ali, Qabd or Sadl](https://www.muwatta.com/ebooks/english/qabd_and_sadl.pdf) | Sadl teaching route, recitation without basmala in the described obligatory-prayer practice, quiet Fajr qunut with preference before ruku, one concluding salam for an individual, rubbing and continuity in wudu |
| Hanbali | [Shaykh Zahed Fettah, The Hanbali Madhhab: prayer, archived by IslamQA](https://islamqa.org/hanbali/thehanbalimadhhab/118328/); [wudu obligations](https://islamqa.org/hanbali/thehanbalimadhhab/118371/) | Hand raising around ruku, quiet basmala, no regular daily Fajr qunut, two concluding salams, mouth/nose/head/ears in wudu |

Consulted on 2026-10-16. References being cited does not imply endorsement of this app by their authors. A specialist religious review and native-language editorial review have not been claimed; these remain part of release review.

## Prayer times

Changing school sets Asr to Hanafi double shadow for Hanafi, or single shadow for the other three schools; both include the object's noon shadow. The regional calculation preset and location are preserved. The user can still change the Asr convention explicitly. This avoids treating a language, country or madhhab as a universal Fajr/Isha angle.

The app continues using its shared, pinned Adhan 1.2.1 calculation engine for both display and alarms. The Singapore preset adds Fajr 20° / Isha 18° for users who need that convention. It does not claim to replace any country's official calendar. Method references: [Adhan project](https://github.com/batoulapps/adhan-java), [PrayTimes calculation definitions](https://praytimes.org/docs/calculation).

## Verification

Run `python3 scripts/verify_content.py` to check all locale resources and placeholders, school isolation, complete translated guide coverage, stable step IDs, media references and the digest of preserved original Hanafi content. Unit tests additionally exercise school switching, Asr schedules, regional-method preservation and the Singapore angles. Android tests exercise actual Android resource loading, RTL/LTR configuration, selected-school navigation and progress.
