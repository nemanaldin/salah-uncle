# نقشهٔ پروژهٔ Salah برای برنامه‌نویس جاوا

این پروژه اکنون Kotlin و Jetpack Compose است و صفحه‌ها با تابع‌های `@Composable` ساخته می‌شوند. لایهٔ UI وضعیت را از ViewModel می‌خواند و رویداد کاربر را به آن برمی‌گرداند؛ ViewModel با Repositoryها برای خواندن JSON و DataStore کار می‌کند؛ منطق مستقل محاسبه و انتخاب در domain قرار دارد و اتصال به قابلیت‌های اندروید در platform است. برای نمونه `DuaRepository` فایل JSON دعاها را به `DuaCatalog` تبدیل می‌کند، بارگذار مستقل ViewModel نتیجه را در `SalahUiState` می‌گذارد و `DuaScreens` همان مدل را نمایش می‌دهد. داده مستقیم از JSON داخل تابع UI خوانده نمی‌شود. تغییر نشان، از UI به ViewModel و سپس Repository تنظیمات می‌رود و جریان وضعیت دوباره UI را به‌روز می‌کند. در Kotlin، `val` متغیر با مرجع ثابت، `var` متغیر قابل انتساب، `data class` مدل داده و تابع suspend عملی است که می‌تواند بدون مسدودکردن رشته منتظر بماند. StateFlow جریان وضعیت و Composable تقریباً همتای تعریف یک View با ورودی وضعیت است؛ بازترسیم را Compose مدیریت می‌کند.

ریشهٔ بررسی‌شده در این محیط `/workspace/salah-uncle` است. تمام مسیرهای فایل در ادامه، مسیر کامل نسبت به همین ریشه‌اند؛ برای استفاده روی رایانهٔ دیگر فقط ریشه عوض می‌شود. وجود تک‌تک فایل‌ها با `Path.is_file()` و جستجوی واقعی در مخزن کنترل شده است. مبنای نقشه `935b519` است؛ بخش فارسیِ این دوره بعداً به همین نقشه افزوده می‌شود.

## نقطهٔ ورود، ViewModel و اتصال لایه‌ها

- `app/src/main/java/com/androidhive/myapplication/MainActivity.kt`: Activity اصلی، تنظیم Compose، اعمال زبان و پوسته و درخواست مجوزهای اندروید.
- `app/src/main/java/com/androidhive/myapplication/SalahApplication.kt`: Application و AppContainer؛ ساخت و نگهداری نمونهٔ Repositoryها، موتور اوقات و هماهنگ‌کننده‌ها.
- `app/src/main/java/com/androidhive/myapplication/SalahViewModel.kt`: وضعیت مشترک صفحه‌ها، بارگذاری مستقل آموزش/شهر/ویدئو/دعا، زمان جاری، رویدادها و تغییر تنظیمات؛ بارگذاری یک بخش، موفقیت بقیه را شرط نمی‌کند.

## خانه، شمارش معکوس و میانبرها

- `app/src/main/java/com/androidhive/myapplication/ui/home/HomeScreen.kt`: عکس سربرگ، نماز بعدی، اوقات روز و شش میانبر تصویری؛ ساعت‌ها از وضعیت واقعی محاسبات می‌آیند.
- `app/src/main/java/com/androidhive/myapplication/ui/home/PrayerCountdown.kt`: تبدیل مدت واقعی به ساعت و دقیقه در خانه و رشتهٔ ثانیه‌ای در صفحهٔ کامل اوقات.
- `app/src/main/java/com/androidhive/myapplication/ui/home/QuickAccessGrid.kt`: کارت‌های میانبر و تصمیم دو ستون یا یک ستون بر اساس عرض و اندازهٔ متن.
- `app/src/main/java/com/androidhive/myapplication/ui/components/Artwork.kt`: انتخاب تصویر مناسب موضوع و سربرگ تصویری.
- `app/src/main/java/com/androidhive/myapplication/ui/components/PrayerArtwork.kt`: آیکون‌های تصویری نمازها و میانبرها.

## اوقات نماز، مکان و اذان

- `app/src/main/java/com/androidhive/myapplication/ui/prayer/PrayerTimesScreen.kt`: نمایش اوقات، انتخاب کشور/شهر، GPS، روش محاسبه و تأیید آن.
- `app/src/main/java/com/androidhive/myapplication/domain/prayer/PrayerEngine.kt`: موتور محاسبهٔ روز و جستجوی نماز بعدی؛ منطق UI در آن قرار ندارد.
- `app/src/main/java/com/androidhive/myapplication/domain/prayer/HomePrayerState.kt`: تفکیک نداشتن مکان، خطای ذخیره‌سازی و خطای محاسبه.
- `app/src/main/java/com/androidhive/myapplication/data/location/CityRepository.kt`: خواندن فهرست شهرها، نام محلی و جستجو و تبدیل شهر به مکان ذخیره‌شده.
- `app/src/main/java/com/androidhive/myapplication/platform/location/DeviceLocation.kt`: گرفتن موقعیت از سرویس‌های اندروید و مدیریت منبع مکان.
- `app/src/main/java/com/androidhive/myapplication/platform/alarm/AlarmCoordinator.kt`: زمان‌بندی و دریافت رویدادهای اعلان نماز.
- `app/src/main/java/com/androidhive/myapplication/platform/alarm/PrayerNotifications.kt`: ساخت اعلان نماز.
- `app/src/main/java/com/androidhive/myapplication/platform/audio/AdhanService.kt`: پخش پس‌زمینهٔ اذان و نشست صوتی.
- `app/src/main/java/com/androidhive/myapplication/ui/ramadan/RamadanScreen.kt`: صفحهٔ رمضان، سحر/افطار و تنظیم یادآور مربوط به آن.

## آموزش، ابزارها و اسلایدها

- `app/src/main/java/com/androidhive/myapplication/ui/lessons/LessonsScreen.kt`: فهرست مستقیم موضوعات، دسته‌ها، جستجو و نشان‌ها و ادامهٔ مطالعه.
- `app/src/main/java/com/androidhive/myapplication/ui/lessons/LessonScreen.kt`: خواندن متن و اسلاید افقی، منابع، نمایش متن و صوت مرتبط؛ اجبار به گذراندن درس قبلی ندارد.
- `app/src/main/java/com/androidhive/myapplication/ui/lessons/ArticleNavigation.kt`: پیمایش بخش‌های مقاله و نگه‌داشتن هدف خواندن.
- `app/src/main/java/com/androidhive/myapplication/data/content/LessonRepository.kt`: خواندن فایل‌های آموزش و ترکیب متن قدیمی، ترجمه و تنظیمات نمایشی.
- `app/src/main/java/com/androidhive/myapplication/data/content/ReadingSlides.kt`: ساخت نماهای مرحله‌ای از محتوای آموزشی.
- `app/src/main/java/com/androidhive/myapplication/data/content/WuduArrangement.kt`: ترکیب نمای خواندن وضو و ذکر مرتبط، با حفظ منشأ بخش‌ها.
- `app/src/main/java/com/androidhive/myapplication/ui/lessons/TrainingScreen.kt`: صفحهٔ ویدئوهای آموزشی آینده و نمایش وضعیت فایل‌های موجود.

## دعاها، جستجو و نشان‌ها

- `app/src/main/java/com/androidhive/myapplication/data/duas/DuaModels.kt`: مدل دسته، زیردسته، دعا، مأخذ، درجه، تعداد و قواعد جستجوی عربی؛ مدل مستقل از Compose است.
- `app/src/main/java/com/androidhive/myapplication/data/duas/DuaRepository.kt`: خواندن و تبدیل JSON دعا به مدل‌ها.
- `app/src/main/java/com/androidhive/myapplication/data/duas/GuideDuas.kt`: شناسهٔ دعاهای مرتبط با جای مشخص در اسلاید نماز و وضوی حنفی.
- `app/src/main/java/com/androidhive/myapplication/ui/duas/DuaScreens.kt`: هفت کارت، زیردسته‌های بازوبسته، جستجو، نشان/تاریخچه، ورق‌زدن، اندازهٔ عربی، کپی/اشتراک و شمارنده.
- `app/src/main/java/com/androidhive/myapplication/data/content/LessonSearch.kt`: جستجوی متن و عنوان بخش‌های آموزش و ساخت نتیجه با مقصد خواندن.
- `app/src/main/java/com/androidhive/myapplication/core/model/SectionBookmark.kt`: مدل نشان در سطح بخش، همراه مذهب/زبان/یادداشت.
- `app/src/main/java/com/androidhive/myapplication/data/settings/SettingsRepository.kt`: ذخیرهٔ ماندگار تنظیمات و نشان‌ها با DataStore و مهاجرت دادهٔ قدیمی.

## یادآور دعا

- `app/src/main/java/com/androidhive/myapplication/ui/duas/DuaReminderSettings.kt`: رضایت، تعداد، بازه و دسته‌های یادآور؛ پیش‌فرض خاموش است.
- `app/src/main/java/com/androidhive/myapplication/domain/duas/DuaReminderLogic.kt`: انتخاب مدخل واجد شرایط، نوبت‌های واقعی و دور تصادفی بدون تکرار با وزن بیشتر نشان‌ها.
- `app/src/main/java/com/androidhive/myapplication/core/model/DuaReminders.kt`: مدل تنظیمات و سابقهٔ دور یادآور.
- `app/src/main/java/com/androidhive/myapplication/platform/duas/DuaReminderCoordinator.kt`: آلارم غیردقیق اندروید، اعلان، دریافت رویداد و بازیابی زمان‌بندی؛ مقصد اعلان شناسهٔ همان دعاست.

## تنظیمات، فونت و رنگ

- `app/src/main/java/com/androidhive/myapplication/ui/settings/SettingsScreen.kt`: صفحهٔ تنظیمات زبان، مکان/محاسبه، صوت و خواندن.
- `app/src/main/java/com/androidhive/myapplication/ui/settings/ReadingFontDialog.kt`: انتخاب فونت، جملهٔ نمونه، پیش‌نمایش و ذخیره.
- `app/src/main/java/com/androidhive/myapplication/core/model/ReadingFont.kt`: شناسهٔ خانواده‌ها و قواعد اندازه/بازنشانی.
- `app/src/main/java/com/androidhive/myapplication/ui/theme/ReadingFonts.kt`: FontFamilyهای داخل برنامه، پوشش خط و سبک خواندن.
- `app/src/main/java/com/androidhive/myapplication/ui/theme/SalahTheme.kt`: پوسته، تایپوگرافی، RTL/LTR و ضریب عمومی متن.
- `app/src/main/java/com/androidhive/myapplication/ui/theme/ColorTokens.kt`: رنگ‌های روشن/تیره و رنگ‌های روی عکس.

## قبله و قطب‌نما

- `app/src/main/java/com/androidhive/myapplication/ui/qibla/QiblaScreen.kt`: نمایش عدد جهت قبله و وضعیت انتظار/نامعتبر/کالیبراسیون/معتبر سنسور.
- `app/src/main/java/com/androidhive/myapplication/domain/qibla/QiblaMath.kt`: محاسبهٔ جهت قبله و زاویهٔ نشانگر.
- `app/src/main/java/com/androidhive/myapplication/domain/qibla/CompassHeading.kt`: تبدیل و قواعد جهت قطب‌نما.
- `app/src/main/java/com/androidhive/myapplication/domain/qibla/HeadingSamples.kt`: اعتبار و حالت دادهٔ سنسور.
- `app/src/main/java/com/androidhive/myapplication/platform/location/HeadingRepository.kt`: ثبت listener سنسورهای اندروید و انتشار نمونه‌ها.

## ناوبری و ابزارهای مشترک

- `app/src/main/java/com/androidhive/myapplication/ui/SalahApp.kt`: NavHost، چهار تب، تنظیمات سربرگ، مقصدهای صفحه‌ها و تابع داخلی ToolsScreen؛ صفحهٔ ابزارها فایل جداگانه ندارد.
- `app/src/main/java/com/androidhive/myapplication/ui/components/Components.kt`: کارت، متن و چیدمان‌های مشترک.
- `app/src/main/java/com/androidhive/myapplication/ui/components/GuideArtwork.kt`: تصویر مرحلهٔ آموزش.
- `app/src/main/java/com/androidhive/myapplication/ui/components/LanguageReadiness.kt`: نمایش وضعیت زبان‌ها طبق سیاست نمایشی.
- `app/src/main/java/com/androidhive/myapplication/core/model/Settings.kt`: مدل تنظیمات، زبان و مذهب و مکان و شناسه‌های انتخاب‌ها.

## فایل‌های داده و رشته‌ها

- `app/src/main/assets/content/hanafi-fa-v1.json`: آموزش‌های قدیمی فارسی؛ بدنه و رسانهٔ آن‌ها را بدون مأخذ و دلیل تغییر ندهید.
- `app/src/main/assets/content/sunni-guides-v1.json`: راهنماهای ساختاریافتهٔ نماز/طهارت با variantهای زبان و مذهب.
- `app/src/main/assets/content/wudu-remembrance-v1.json`: مدخل مشترک ذکر پس از وضو و مرجع آن.
- `app/src/main/assets/duas/duas-v1.json`: دسته‌ها، زیردسته‌ها، ۷۰ موضوع، عربی، ترجمه، منابع، درجه و فیلدهای داخلی دعا.
- `app/src/main/assets/duas/owner-texts.json`: سه متن دقیق مالک و هش آن‌ها؛ متن عربی‌شان تغییر نمی‌کند.
- `app/src/main/assets/duas/source-checks.json`: اثر انگشت متن‌های عربیِ دارای مأخذ در تحویل قبلی.
- `app/src/main/assets/locations.json`: فهرست مکان‌های قدیمی با شناسه‌های محفوظ.
- `app/src/main/assets/geography/cities-v1.bin`: دادهٔ فشردهٔ شهرهای جهان؛ پسوند bin دارد، داخلش gzip است.
- `app/src/main/assets/geography/place-names-v1.json`: نام شهرها و ناحیه‌ها برای زبان‌ها.
- `app/src/main/assets/training/manifest.json`: فهرست ویدئوها و دامنهٔ زبان/مذهب آن‌ها؛ ساخت انیمیشن در این پروژه انجام نمی‌شود.
- `app/src/main/assets/metadata/review-status.properties`: وضعیت داخلی آمادگی زبان و variant آموزش.
- `app/src/main/res/values/ui_strings.xml`: رشته‌های پیش‌فرض فارسیِ رابط؛ نام واقعی ui_strings.xml است، نه strings_ui.xml.
- `app/src/main/assets/licenses/fonts/manifest.json`: نام، مأخذ، مجوز و هش فونت‌های آفلاین.
- `scripts/verify_content.py`: کنترل یکپارچگی رشته‌ها، داده، رسانه، زبان و جغرافیا و اجرای کنترل‌های تخصصی.
- `scripts/verify_duas.py`: کنترل متن مالک، مأخذ، درجه و ساختار مجموعهٔ دعا.
- `scripts/export_dua_review.py`: استخراج پروندهٔ فیلدهای باز دعا برای مطالعهٔ مالک.

فایل‌های ترجمهٔ واقعی:

- `app/src/main/assets/translations/editorial-v1.json`: لایهٔ ویرایشی و عنوان‌ها؛ با فایل ترجمهٔ دعا متفاوت است.
- `app/src/main/assets/translations/legacy-ar-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-bn-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-en-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-fr-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-id-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-ms-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-ps-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-tr-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.
- `app/src/main/assets/translations/legacy-ur-v1.json`: بدنهٔ ترجمهٔ آموزش قدیمی.

رشته‌های سایر زبان‌ها که در این دوره نباید تغییر کنند:

- `app/src/main/res/values-ar/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-bn/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-en/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-fr/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-in/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-ms/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-ps/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-tr/ui_strings.xml`: رشته‌های رابط همان زبان.
- `app/src/main/res/values-ur/ui_strings.xml`: رشته‌های رابط همان زبان.

پوشهٔ `app/src/main/res/font/` فایل فونت‌ها، `app/src/main/res/drawable-nodpi/` عکس و برگه‌های تصویر و `app/src/main/res/raw/` صوت‌های قدیمی را دارد. وجود این پوشه‌ها نیز کنترل شد.

## تغییر کوچک چطور؟

- تغییر متن یک دعا: شناسه را در دادهٔ دعا پیدا کنید؛ متن مالک را تغییر ندهید. برای متن تازه مأخذ خوانده‌شده و درجه/نوع متن ثبت کنید، وضعیت داخلی را نگه دارید و کنترل متن/منبع را اجرا کنید. در لایهٔ فارسی این دوره، دادهٔ فارسی جدا می‌شود تا زبان‌های دیگر دست‌نخورده بمانند؛ مسیر واقعی آن پس از ساخت در ادامهٔ همین سند ثبت می‌شود.
- عوض‌کردن عنوان: عنوانِ همان شناسه و فقط کلید fa را در داده تغییر دهید. عنوان دسته/زیردسته داخل بخش خودش است؛ برچسب دکمهٔ ثابت داخل ui_strings.xml است. شناسهٔ موضوع را برای عوض‌کردن عنوان تغییر ندهید، چون نشان‌ها و مقصد اعلان از شناسه استفاده می‌کنند.
- تغییر رنگ: رنگ پایه را در ColorTokens.kt و کاربرد پوسته را در SalahTheme.kt دنبال کنید؛ رنگ معنایی را تغییر دهید تا صفحه‌ها یکپارچه بمانند.
- ترتیب میانبرها: ترتیب لیست shortcuts در HomeScreen.kt را عوض کنید. QuickAccessGrid فقط چیدمان را می‌سازد؛ مقصد و ترتیب از خانه می‌آید.
- افزودن موضوع دعا: شناسهٔ یکتا، category و section درست، عنوان، متن/معنی/تلفظ و منابع در داده اضافه کنید و شناسه را در duaIds زیردسته بگذارید. جایگاه نماز/وضو را تنها با مأخذ عمل حنفی در GuideDuas.kt متصل کنید. وضعیت داخلی pending_owner_review به معنی تأیید عالم نیست. سپس کنترل محتوا و تست و Build را اجرا کنید.

## دستورهای کنترل

پس از هر مرحله: `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin` و سپس `python3 scripts/verify_content.py`. نام واقعی کنترل پروژه verify_content.py است؛ در پرامپت از آن با عنوان content_verify یاد شده است. این دستورها تست واحد و کامپایل رابط را پوشش می‌دهند؛ ادعای تست گوشی یا سنسور نیستند.
