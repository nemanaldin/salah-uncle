package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.*

enum class IllustrationStatus { DRAFT_PENDING_OWNER_REVIEW }
enum class StageArtAsset { ATLAS, PROSTRATION }
enum class StageArtKind { SYMBOLIC, ORGAN, POSE }
data class StageArt(val sourceLessonId: String, val stepId: String, val cell: Int,
    val kind: StageArtKind, val altFa: String, val altEn: String, val altTr: String,
    val displayable: Boolean = true, val requiresPoseReview: Boolean = false,
    val status: IllustrationStatus = IllustrationStatus.DRAFT_PENDING_OWNER_REVIEW, val asset: StageArtAsset = StageArtAsset.ATLAS) {
    fun alt(language: AppLanguage) = when (language) {
        AppLanguage.PERSIAN -> altFa
        AppLanguage.TURKISH -> altTr
        else -> altEn
    }
}
object StageArtCatalog {
    val entries = listOf(
        StageArt("prayer-hanafi", "preparation", 0, StageArtKind.SYMBOLIC, "جانماز و ظرف آب برای بخش آمادگی نماز.", "Prayer mat and water jug for preparation.", "Hazırlık bölümü için seccade ve su kabı.", displayable = true, requiresPoseReview = false),
        StageArt("prayer-hanafi", "opening", 1, StageArtKind.ORGAN, "دو دست بالا آمده برای آغاز نماز؛ جای بستن دست‌ها نمایش داده نشده است.", "Raised hands for the opening; folded-hand placement is not depicted.", "Başlangıç için kaldırılmış eller; ellerin bağlanma yeri gösterilmez.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "recitation", 2, StageArtKind.SYMBOLIC, "کتاب بدون نوشته کنار جانماز برای بخش قرائت.", "A blank book beside a prayer mat for recitation.", "Kıraat bölümü için seccadenin yanında yazısız kitap.", displayable = true, requiresPoseReview = false),
        StageArt("prayer-hanafi", "bowing", 3, StageArtKind.POSE, "پیکرهٔ پوشیده و بدون چهره در رکوع؛ جزئیات حالت نیازمند بازبینی است.", "A clothed faceless figure bowing; posture details await review.", "Rükûda giyinik, yüz çizilmemiş figür; duruş ayrıntıları inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "standing", 4, StageArtKind.POSE, "ایستادن آرام پس از رکوع با پیکرهٔ پوشیده و بدون چهره.", "A clothed faceless figure standing calmly after bowing.", "Rükûdan sonra sakince ayakta duran, giyinik ve yüz çizilmemiş figür.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "prostrations", 5, StageArtKind.POSE, "پیکرهٔ پوشیده و بدون چهره در سجده؛ جزئیات حالت نیازمند بازبینی است.", "A clothed faceless figure prostrating; posture details await review.", "Secdede giyinik, yüz çizilmemiş figür; duruş ayrıntıları inceleme bekliyor.", displayable = true, requiresPoseReview = true, asset = StageArtAsset.PROSTRATION),
        StageArt("prayer-hanafi", "second-unit", 6, StageArtKind.POSE, "پیکرهٔ ایستاده با نشانهٔ تکرار برای بخش رکعت دوم.", "A standing figure with a repeat symbol for the second unit.", "İkinci rekât bölümü için tekrar simgesiyle ayakta duran figür.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "sitting", 7, StageArtKind.POSE, "پیکرهٔ پوشیده و بدون چهره در نشستن؛ جزئیات حالت نیازمند بازبینی است.", "A clothed faceless seated figure; posture details await review.", "Oturan giyinik, yüz çizilmemiş figür; duruş ayrıntıları inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "ending", 8, StageArtKind.POSE, "پیکرهٔ نشسته و دو پیکان چرخش برای بخش پایان نماز.", "A seated figure and two turning arrows for the ending.", "Namazın bitiş bölümü için oturan figür ve iki dönüş oku.", displayable = true, requiresPoseReview = true),
        StageArt("prayer-hanafi", "daily-units-and-times", 9, StageArtKind.SYMBOLIC, "نشانه‌های خورشید و ماه بالای جانماز برای بخش اوقات نماز.", "Sun and moon symbols above a prayer mat for daily times.", "Günlük vakitler bölümü için seccade üzerinde güneş ve ay simgeleri.", displayable = true, requiresPoseReview = false),
        StageArt("purification-hanafi", "intention-and-hands", 10, StageArtKind.ORGAN, "نمای نزدیک دست‌ها و مچ‌ها زیر جریان ملایم آب.", "Close-up of hands and wrists under gentle water.", "Hafif su altında ellerin ve bileklerin yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("purification-hanafi", "mouth-nose-and-face", 11, StageArtKind.ORGAN, "نمادهای نزدیک دهان و بینی و سر بی‌چهره در حال شستن صورت.", "Close-up mouth and nose symbols and a faceless head being washed.", "Ağız ve burun yakın görünüm simgeleri ve yıkanan, yüz çizilmemiş baş.", displayable = true, requiresPoseReview = true),
        StageArt("purification-hanafi", "arms", 12, StageArtKind.ORGAN, "نمای نزدیک شستن بازوها همراه آرنج با آب.", "Close-up of arms including elbows being washed.", "Dirsekler dahil kolların yıkanmasının yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("purification-hanafi", "head-ears-and-feet", 13, StageArtKind.ORGAN, "نماهای نزدیک مسح سر و گوش و شستن پا؛ بدون چهره.", "Close-ups of head and ear wiping and foot washing; no face.", "Baş ve kulak meshinin ve ayak yıkamanın yakın görünümü; yüz çizilmemiştir.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu_text1", 14, StageArtKind.SYMBOLIC, "ظرف آب و حولهٔ تاخورده برای مقدمهٔ وضو.", "Water jug and folded towel for the wudu introduction.", "Abdest giriş bölümü için su kabı ve katlanmış havlu.", displayable = true, requiresPoseReview = false),
        StageArt("wudu", "vuzu1", 15, StageArtKind.SYMBOLIC, "قطرهٔ آب کنار کتاب بستهٔ بدون نوشته برای ذکر آغاز.", "A water drop beside a blank closed book for the opening remembrance.", "Başlangıç zikri için yazısız kapalı kitap yanında su damlası.", displayable = true, requiresPoseReview = false),
        StageArt("wudu", "vuzu_text2", 16, StageArtKind.SYMBOLIC, "قطرهٔ آب و نشانهٔ قلب برای بخش نیت.", "A water drop and heart symbol for intention.", "Niyet bölümü için su damlası ve kalp simgesi.", displayable = true, requiresPoseReview = false),
        StageArt("wudu", "vuzu2", 17, StageArtKind.ORGAN, "نمای نزدیک شستن هر دو دست تا مچ.", "Close-up of both hands being washed to the wrists.", "İki elin bileklere kadar yıkanmasının yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu3", 18, StageArtKind.ORGAN, "نمای نزدیک نماد دهان و دست آب‌گرفته؛ بدون چهره.", "An isolated mouth symbol and a cupped hand with water; no face.", "Yüz çizilmeden ağız simgesi ve su tutan avucun yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu4", 19, StageArtKind.ORGAN, "نمای نزدیک نماد بینی و دست آب‌گرفته؛ بدون چهره.", "An isolated nose symbol and a hand with water; no face.", "Yüz çizilmeden burun simgesi ve su tutan elin yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu5", 20, StageArtKind.ORGAN, "سر بدون اجزای چهره و دست‌های خیس برای شستن صورت.", "A head without facial features and wet hands for face washing.", "Yüz yıkama için yüz ayrıntıları çizilmemiş baş ve ıslak eller.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu6", 21, StageArtKind.ORGAN, "شستن بازو در بخش دست راست؛ سمت اندام نیازمند بازبینی است.", "Arm washing for the right-arm section; anatomical side awaits review.", "Sağ kol bölümü için kol yıkama; anatomik yön inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu7", 22, StageArtKind.ORGAN, "شستن بازو در بخش دست چپ؛ سمت اندام نیازمند بازبینی است.", "Arm washing for the left-arm section; anatomical side awaits review.", "Sol kol bölümü için kol yıkama; anatomik yön inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu8", 23, StageArtKind.ORGAN, "نمای نزدیک پشت و بالای سر و گوش در حال مسح؛ بدون چهره.", "Close-up of the back and crown of the head and ear being wiped; no face.", "Yüz çizilmeden başın arka ve üst kısmı ile kulağın meshinin yakın görünümü.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu9", 24, StageArtKind.ORGAN, "نمای پشت گردن و انگشت‌ها؛ تصویر متن قدیمی و نیازمند بازبینی منبع.", "Back of neck and fingers; legacy-source illustration pending source review.", "Ense ve parmaklar; eski kaynak görseli, kaynak incelemesi bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu10", 25, StageArtKind.ORGAN, "شستن پا و قوزک در بخش پای راست؛ سمت اندام نیازمند بازبینی است.", "Foot and ankle washing for the right-foot section; anatomical side awaits review.", "Sağ ayak bölümü için ayak ve bilek yıkama; anatomik yön inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu11", 26, StageArtKind.ORGAN, "شستن پا و قوزک در بخش پای چپ؛ سمت اندام نیازمند بازبینی است.", "Foot and ankle washing for the left-foot section; anatomical side awaits review.", "Sol ayak bölümü için ayak ve bilek yıkama; anatomik yön inceleme bekliyor.", displayable = true, requiresPoseReview = true),
        StageArt("wudu", "vuzu_text3", 27, StageArtKind.SYMBOLIC, "بانداژ تمیز روی ساعد کنار قطره‌های آب؛ بدون زخم نمایان.", "A clean forearm dressing beside water drops; no visible wound.", "Su damlaları yanında ön kolda temiz bandaj; görünür yara yok.", displayable = true, requiresPoseReview = false),
        StageArt("wudu-remembrance", "after-wudu", 28, StageArtKind.SYMBOLIC, "قطرهٔ آب و کتاب بدون نوشته برای ذکر پایانی وضو.", "A water drop and blank book for the post-wudu remembrance.", "Abdest sonrası zikir için su damlası ve yazısız kitap.", displayable = true, requiresPoseReview = false),
        StageArt("ghusl", "ghosl", 29, StageArtKind.SYMBOLIC, "نماد دوش، قطره‌های آب و حوله؛ هیچ بدن یا برهنگی نمایش داده نمی‌شود.", "Symbolic shower, water drops and towel; no body or nudity.", "Simgesel duş, su damlaları ve havlu; beden veya çıplaklık gösterilmez.", displayable = true, requiresPoseReview = false),
    )
    private val bySource = entries.associateBy { it.sourceLessonId to it.stepId }
    fun find(lessonId: String, stepId: String) = bySource[lessonId to stepId]
}
fun stageArtwork(lessonId: String, step: LessonStep, school: School): StageArt? =
    if (school != School.HANAFI || !isSlideGuide(lessonId, school)) null
    else StageArtCatalog.find(step.originLessonId ?: lessonId, step.id)

data class StageFrame(val left: Int, val top: Int, val width: Int, val height: Int)
/** Integer edges partition even an image whose width is not divisible by six. */
fun stageFrame(cell: Int, width: Int, height: Int): StageFrame {
    require(cell in 0 until 30 && width >= 6 && height >= 5)
    val column = cell % 6; val row = cell / 6
    val left = column * width / 6; val top = row * height / 5
    return StageFrame(left, top, (column + 1) * width / 6 - left, (row + 1) * height / 5 - top)
}
