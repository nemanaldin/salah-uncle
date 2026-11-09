# School-specific content review — AI-assisted, not approval

Date: 2026-11-09. App: 2.7.0/code 14. Content baseline: `a09fbbd23cf3f1026e09ba0be56d8079e9a86849`. All instructional JSON and translation sources remain byte-for-byte unchanged during these fixes.

**Every item below is pending review by a qualified scholar of that school. No item is approved.** Confidence describes this limited comparison, not religious authority. Proposed wording is a discussion draft in this report only; it must not be copied into the app without the owner's and qualified scholar's review. The three stable-language `reviewed` metadata values requested for release readiness do not approve any ruling or translation.

## Scope and reproducible app evidence

- `app/src/main/assets/content/hanafi-fa-v1.json`: `tayammum/HANAFI/fa/tayammom` and `travel/HANAFI/fa/mosafer`.
- `app/src/main/assets/translations/legacy-en-v1.json`: the corresponding English bodies. `LessonRepository.withTranslations` applies these overlays. `editorial-v1.json` has no Persian correction for either of these two steps, so the Persian discrepancies still reach the reader.
- `app/src/main/assets/content/sunni-guides-v1.json`: `prayer-{shafii,maliki,hanbali}` (10 sections each), `purification-{shafii,maliki,hanbali}` (4 sections each), in all ten languages.
- `app/src/main/assets/content/wudu-remembrance-v1.json`: `wudu-remembrance`, each school's `after-wudu` and `shared-source` sections, in all ten languages. The three schools share the same Arabic remembrance and cited link.

Persian and English introductions and school-specific sections were inspected. This is not a full semantic/native-language audit of all ten languages, Arabic recitation pronunciation, or recordings. Structural tests establish availability and school isolation, not religious correctness. Each of the other three schools has only these three topics; that does not constitute a complete school curriculum. Ghusl, tayammum, invalidators, traveller/congregational rules, illness and detailed remedial prayer rules require separate school-specific coverage before any claim of completeness.

## Standard references actually consulted

References are identified by book and chapter, not invented edition/page numbers. Online transcriptions are not certified critical editions; precise edition/pagination remains **unverified**. No modern fatwa site is used here as a substitute for these standard books.

- **H1:** al-Quduri, *Mukhtasar al-Quduri*, **Bab al-Tayammum** and **Bab Salat al-Musafir**, [Arabic transcription](https://arabic-keyboard.info/books/ar/read-book/مختصر-القدوري). Both chapter texts were accessible.
- **M1:** Ibn Abi Zayd al-Qayrawani, *al-Risala*, **chapter 4, purification**, [IIUM translation](https://www.iium.edu.my/deed/lawbase/risalah_maliki/book04.html), especially 4.4–4.6. The page separates translated base text from bracketed explanatory material; the latter must not be quoted as Ibn Abi Zayd's own words.
- **M2:** the same *al-Risala*, **chapter 10, prayer**, [IIUM translation](https://www.iium.edu.my/deed/lawbase/risalah_maliki/book10.html), especially 10.1–10.3. The distinction between base text and commentary matters for the preferred position of qunut.
- **S1:** Abu Shuja, *al-Ghaya wa al-Taqrib*, **Kitab al-Tahara** (obligations/sunnas of wudu) and **Kitab al-Salah** (conditions, pillars, sunnas), [Arabic transcription](https://www.arabic-keyboard.info/books/ar/read-book/متن-أبي-شجاع-المسمى-الغاية-والتقريب).
- **B1:** al-Hajjawi, *Zad al-Mustaqni fi Ikhtisar al-Muqni*, **Bab Sunan al-Wudu**, **Bab Furud al-Wudu wa Sifatihi**, **Bab Sifat al-Salah**, and **Bab Salat al-Tatawwu**, [Arabic transcription](https://arabic-keyboard.info/books/ar/read-book/زاد-المستقنع-في-اختصار-المقنع). The base text was accessible; one repeat request to the www mirror was rate-limited. No inference is made from that failed request.

*Minhaj al-Talibin*, *Mukhtasar Khalil*, *al-Hidaya*, *Maraqi al-Falah* and *Radd al-Muhtar* are possible qualified-review cross-checks, but no uninspected passage, quotation or page from them is claimed here. Where the sources above do not establish a point, the result is **unverified**, not agreement by default.

## A. Hanafi discrepancies

### A1. Tayammum: fixed two-kilometre condition

**Status:** pending review by a qualified Hanafi scholar.

**App says:** Persian `tayammom` permits substitution when there is no water and the user is at least 2 km away. It also lists vital drinking needs, danger and credible illness. English describes inability to obtain/use water safely after reasonable searching and qualifies the old two-kilometre figure as an approximate example requiring scholarly guidance. The Persian opening also groups entering a mosque with acts needing purification without distinguishing states; that additional wording needs review.

**Standard source:** H1's tayammum chapter gives approximately a mile or more for water remoteness and addresses illness, access and seeking water. It does not establish a universal exact metric threshold of 2.000 km. A classical mile's modern conversion is not independently settled by this transcription.

**Agreement:** partial. The two languages are materially inconsistent about whether the number is a rigid condition. English is more qualified, but this review does not approve every qualification.

**Proposed Persian draft:** «تیمم در صورت فراهم بودن شرایط فقه حنفی، مانند نبود آبِ قابل دسترس یا زیانِ معتبرِ استعمال آن، مطرح می‌شود. فاصلهٔ تقریبی ذکرشده از آب را یک حد دقیق و همیشگیِ دو کیلومتر تلقی نکنید؛ شرایط جست‌وجو، دسترسی ایمن و نیاز ضروری به آب باید جداگانه بررسی شوند.» Have the scholar supply the intended distance convention and precise exceptions before publication.

**Confidence:** high that the app languages conflict; moderate on this source comparison; metric conversion and the opening mosque statement **unverified**. After approval, prefer an explicit Persian editorial overlay for `tayammom`, preserving the archived source, and review all nine translations against it.

### A2. Traveller's prayer: evidence and residence duration

**Status:** pending review by a qualified Hanafi scholar.

**App says:** Persian `mosafer` quotes al-Baqara 2:185 and then says shortening follows from this verse; it gives 82 km, leaving the town boundary and a 90 km example, without a residence-duration condition. English identifies the verse's fasting context, separates qasr, qualifies the distance estimate and mentions an intended stay of 15 days or more.

**Standard source:** H1's traveller chapter describes a qualifying journey, shortening four-unit obligatory prayers, leaving settlement buildings, and completing prayers when intending residence in a town for at least fifteen days. Its classical journey measure is not a verified exact 82 km conversion.

**Agreement:** partial. Boundary/shortening descriptions broadly align; the Persian presentation omits an essential residence qualification and conflates the fasting quotation with the detailed legal basis. The existing English qualification must not be treated as comprehensive travel advice.

**Proposed Persian draft:** «آیهٔ نقل‌شده در سیاق روزه است. احکام تفصیلی قصر نماز در سفر را جداگانه و بر اساس منابع فقه حنفی بررسی کنید. در سفر واجد شرایط، فرض چهاررکعتی ظهر، عصر و عشا دو رکعت می‌شود؛ صبح و مغرب تغییر نمی‌کنند. پس از خروج از محدودهٔ آبادی، مدت اقامتِ مورد قصد نیز مؤثر است؛ قصد اقامت پانزده روز یا بیشتر در یک شهر حکم اتمام دارد. دربارهٔ مسافت معتبر و وضعیت خاص سفر از عالم حنفی بپرسید.»

**Confidence:** high on the observed omission and H1's fifteen-day statement; exact modern distance and compound/uncertain stays **unverified**. Future approved changes belong in the `mosafer` editorial overlay and aligned translations, not an automatic replacement now.

## B. Other schools: three topics each

### B1. Shafi'i beginner prayer guide

**Status:** pending review by a qualified Shafi'i scholar.

**App says:** `prayer-shafii` includes basmala as part of al-Fatiha, hand raising at opening/bowing/rising, Fajr qunut after rising in the second unit, final blessings and right/left salutations. Daily obligatory units are 2/4/4/3/4. It presents reciting the bowing/prostration remembrance three times without distinguishing legal categories.

**Standard source:** S1 lists Fatiha including basmala among pillars; first salutation among pillars, second among recommended forms; Fajr qunut among sunnas. The exact timing/wording of the supplied qunut was not independently established from that passage: **unverified**.

**Agreement/completeness:** main outline broadly aligns, with important omissions in distinguishing obligatory from recommended acts. A beginner cannot tell what omission affects validity. Audible/quiet units, sitting details, recitation accuracy and recovery from mistakes need explicit school review. This is an individual-prayer introduction, not a full Shafi'i manual.

**Proposed draft addition:** “This is an introductory sequence. Required acts and recommended additions are identified separately; do not treat every threefold repetition or the second salutation as a pillar.” Add a scholar-reviewed legal-category annotation and independently verify the Fajr-qunut passage before revising it.

**Confidence:** moderate for the checked outline; low/unverified for exhaustive details and all translations. Likely future file: `sunni-guides-v1.json`, this lesson's school/language variants, after approval only.

### B2. Shafi'i wudu

**Status:** pending review by a qualified Shafi'i scholar.

**App says:** `purification-shafii` instructs intention at face washing, full-head/ear wiping, threefold washing and continuity. Its opening mistakenly describes the scope as “daily prayer performed alone” although this is a wudu topic.

**Standard source:** S1 distinguishes six obligations, including intention at face washing, wiping part of the head and order, from recommended acts such as fuller wiping, repetition and continuity.

**Agreement/completeness:** the described fuller procedure can coexist with the minimum, but failing to label the distinction may teach optional acts as requirements. Nullifiers, barriers and detailed face/arm boundaries remain incomplete.

**Proposed draft:** “Minimum requirements and recommended completion are shown separately. Full-head wiping and repeated washing in this guide describe recommended completion, not additional obligations.” Replace the misplaced prayer-scope sentence with a scholar-reviewed wudu introduction.

**Confidence:** moderate/high on this limited comparison; native-language equivalence **unverified**. Future location: the four sections of `purification-shafii`; no app wording changed.

### B3. Shafi'i shared remembrance after wudu

**Status:** pending review by a qualified Shafi'i scholar.

**App says:** `wudu-remembrance/SHAFII` gives the shahada after wudu, attributes it to “Sahih Muslim 234b”, states it is not exclusive to one school and does not attribute separate limb prayers to the Prophet.

**Standard source/comparison:** the consulted S1 purification passage does not establish the exact full formula or the app's numbered attribution. School-specific treatment and precise attribution are **unverified** under this review's standard-school-reference restriction. Absence from that passage is not evidence of rejection.

**Proposed editorial draft:** “Shared remembrance after wudu; exact attribution and school-specific presentation pending verification.” Retain or revise the Arabic only after source verification; add no new limb-specific supplications by inference.

**Confidence:** high on what the asset contains; low on independent source verification. Future file: this school's two sections in `wudu-remembrance-v1.json`, subject to review.

### B4. Maliki beginner prayer guide

**Status:** pending review by a qualified Maliki scholar.

**App says:** `prayer-maliki` follows sadl while acknowledging folding, omits basmala in the stated obligatory-prayer practice, uses quiet Amin, prefers quiet Fajr qunut before ruku while allowing after, and ends the individual prayer with one salutation. It supplies a Maliki tashahhud and qunut.

**Standard source:** M2 describes the individual salutation once with a slight right turn and permits qunut before/after ruku. Its base text presents after-ruku first; the bracketed explanation identifies a preference for before-ruku. These are not interchangeable authorial statements.

**Agreement/completeness:** much of the outline is consistent with M2, but the claim about the school's preferred qunut timing and the sadl/qabd qualification need a qualified Maliki cross-check. Neither should be declared wrong merely because another valid transmission appears. Rubbing/continuity are wudu topics; obligatory/recommended prayer acts, posture and audible/quiet units still need clearer coverage.

**Proposed draft:** “This introductory individual-prayer guide follows the specified Maliki practice; valid transmissions are acknowledged separately.” Annotate the qunut preference with a verified standard-school passage and distinguish it from the Risala base text. Do not replace a valid transmission automatically.

**Confidence:** moderate on the checked salutation/allowance; preferred-position attribution, complete Arabic formula comparison and ten-language equivalence **unverified**. Future file: `prayer-maliki` variants, after scholar review.

### B5. Maliki wudu

**Status:** pending review by a qualified Maliki scholar.

**App says:** `purification-maliki` includes intention, whole-head wiping, rubbing washed limbs and continuity; it orders mouth/nose, face, arms, head/ears and feet, generally three times. It repeats the misplaced prayer-scope opening.

**Standard source:** M1 describes whole-head wiping, washing with rubbing, and distinguishes required from recommended completion; thorough washing need not always be repeated three times. Its bracketed commentary must remain distinguished from the base text.

**Agreement/completeness:** the stated rubbing/whole-head procedure broadly aligns. Readers still need a clear obligations/sunnas distinction, continuity exceptions and nullifiers; the introduction is contextually wrong.

**Proposed draft:** “This introduction explains Maliki wudu. Required washing/rubbing and recommended repetitions are labelled separately; repetition does not replace complete coverage.” A scholar must supply the exact continuity qualification rather than a newly invented exception.

**Confidence:** moderate on the checked procedure; full legal treatment and translation equivalence **unverified**. Future file: `purification-maliki`, not edited now.

### B6. Maliki shared remembrance after wudu

**Status:** pending review by a qualified Maliki scholar.

**App says:** the same Arabic shahada and “Sahih Muslim 234b” link as the other schools, without a Maliki-only claim or limb-specific prayers.

**Standard source/comparison:** M1's consulted wudu chapter does not independently establish this exact formula as a distinct Maliki practice. Its precise attribution and treatment in the selected standard Maliki references are **unverified**. No conclusion of rejection follows.

**Proposed editorial draft:** “Shared remembrance after wudu; attribution and Maliki presentation pending verification.” Avoid presenting a generic source as proof of a unique Maliki ruling. Do not create substitute Arabic or translate a new formula in this task.

**Confidence:** high on asset equality; low on independent school-specific verification. Future file: `wudu-remembrance/MALIKI` variants, after qualified review.

### B7. Hanbali beginner prayer guide

**Status:** pending review by a qualified Hanbali scholar.

**App says:** `prayer-hanbali` has quiet basmala, audible Amin in audible prayer, right/left salutations and no daily Fajr qunut; witr and calamity qunut are separate. It instructs three recitations in bowing/prostration.

**Standard source:** B1 describes quiet basmala, both salutation directions, distinguishes pillars/obligatory acts/sunnas and treats regular qunut in witr separately from calamities. It distinguishes a required remembrance from recommended threefold repetition.

**Agreement/completeness:** broad sequence agrees with the checked B1 passages. The app does not distinguish the legal categories clearly, so a correct full procedure can still mislead a novice. Conditions, audible/quiet units and mistakes/omissions are not complete.

**Proposed draft:** “Required remembrances and recommended repetitions are distinguished; not every repeated phrase has the same legal status.” Have a Hanbali scholar annotate the existing sequence rather than importing another school's rules.

**Confidence:** moderate on the limited outline; exact recitation variants, every exception and ten-language fidelity **unverified**. Future file: `prayer-hanbali` variants only after approval.

### B8. Hanbali wudu

**Status:** pending review by a qualified Hanbali scholar.

**App says:** `purification-hanbali` explicitly includes mouth/nose, whole head/ears and Bismillah when remembered, with intention, order and no long gaps. Its generic introduction again refers to prayer rather than purification.

**Standard source:** B1 includes mouth/nose with the face, ears with the head, order and continuity; it addresses intention as a condition and naming Allah when remembered. Repeated washing is not the same as an added obligation.

**Agreement/completeness:** the principal procedure aligns with these checked points, but the legal status of order/continuity and repetitions needs explicit annotation. Waking-related handwashing, invalidators and exceptions need coverage before completeness is claimed.

**Proposed draft:** “Hanbali wudu: required acts, conditions and recommended completion are shown separately.” Correct the misplaced scope sentence after review, and do not universalize an exception from a different school.

**Confidence:** moderate/high on checked points; exhaustive detail **unverified**. Future file: `purification-hanbali`, unchanged in this run.

### B9. Hanbali shared remembrance after wudu

**Status:** pending review by a qualified Hanbali scholar.

**App says:** the identical after-wudu shahada and common-source explanation, citing “Sahih Muslim 234b”.

**Standard source/comparison:** B1's wudu description refers generally to saying the transmitted remembrance, but the consulted passage does not itself print the app's full formula or verify that numbered attribution. The exact correspondence remains **unverified**, not approved by the generic reference.

**Proposed editorial draft:** “Shared remembrance after wudu; precise text and attribution pending verification for this presentation.” Keep limb-specific supplications separate and unapproved; no additional formula is proposed.

**Confidence:** high on asset contents; low on exact independent attribution. Future file: `wudu-remembrance/HANBALI` variants after qualified review.

## Publication decisions still required

All eleven items remain pending review by a qualified scholar of the relevant school. Priority is A1/A2's cross-language inconsistency, then per-school required/recommended distinctions and missing scope, then exact recitation/remembrance attribution, then native-language review of each approved source version. No complete school course, comprehensive translation approval or scholar sign-off is claimed. Any future accepted revision should record the book/chapter, verified edition if available, reviewer, date and affected variant IDs, followed by separate native-language review. This report is the only place containing proposed religious wording; app assets were not changed.
