package com.androidhive.myapplication.data.content

import android.content.Context
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.School
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class LessonStep(val id: String, val body: String, val audio: String?, val illustrations: List<String>)
data class LessonSource(val title: String, val url: String)
data class LessonVariant(val school: School, val language: String, val steps: List<LessonStep>, val sources: List<LessonSource> = emptyList())
data class Lesson(
    val id: String, val titleFa: String, val titleEn: String, val category: String,
    val source: String, val variants: List<LessonVariant>, val titles: Map<String, String> = emptyMap(),
) {
    fun title(language: AppLanguage) = titles[language.tag] ?: if (language == AppLanguage.PERSIAN) titleFa else titleEn
    // Content language is explicit; a different school's content is never used as a fallback.
    fun variant(school: School, language: AppLanguage? = null): LessonVariant? =
        variants.firstOrNull { it.school == school && it.language == language?.tag }
            ?: variants.firstOrNull { it.school == school }
}
data class TrainingVideo(val id: String, val titleFa: String, val titleEn: String, val assetPath: String) {
    fun title(language: AppLanguage) = if (language == AppLanguage.PERSIAN) titleFa else titleEn
}

class LessonRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private val mutex = Mutex()
    private var cached: List<Lesson>? = null

    suspend fun load(): List<Lesson> = withContext(Dispatchers.IO) {
        mutex.withLock {
            cached ?: assets.list("content").orEmpty().filter { it.endsWith(".json") }.sorted().flatMap { file ->
                parse(assets.open("content/$file").bufferedReader().use { it.readText() })
            }.also { lessons ->
                require(lessons.map { it.id }.distinct().size == lessons.size)
                cached = lessons
            }
        }
    }

    suspend fun videos(): List<TrainingVideo> = withContext(Dispatchers.IO) {
        val json = JSONObject(assets.open("training/manifest.json").bufferedReader().use { it.readText() })
        val array = json.getJSONArray("videos")
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val path = item.getString("assetPath")
            require(path.startsWith("training/") && !path.contains(".."))
            TrainingVideo(item.getString("id"), item.getString("titleFa"), item.getString("titleEn"), path)
        }.also { videos ->
            require(videos.map { it.id }.distinct().size == videos.size)
        }
    }

    private fun parse(text: String): List<Lesson> {
        val json = JSONObject(text)
        require(json.getInt("schemaVersion") == 1)
        val array = json.getJSONArray("lessons")
        val lessons = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val variants = item.getJSONArray("variants")
            Lesson(item.getString("id"), item.getString("titleFa"), item.getString("titleEn"),
                item.getString("category"), item.getString("source"),
                (0 until variants.length()).map { variantIndex ->
                    val variant = variants.getJSONObject(variantIndex)
                    val steps = variant.getJSONArray("steps")
                    LessonVariant(School.valueOf(variant.getString("school")), variant.getString("language"),
                        (0 until steps.length()).map { stepIndex ->
                            val step = steps.getJSONObject(stepIndex)
                            val images = step.getJSONArray("illustrations")
                            LessonStep(step.getString("id"), step.getString("body"), step.optString("audio").takeIf { it.isNotBlank() },
                                (0 until images.length()).map(images::getString))
                        }.also { parsed -> require(parsed.isNotEmpty() && parsed.map { it.id }.distinct().size == parsed.size) },
                        variant.optJSONArray("sources")?.let { sources ->
                            (0 until sources.length()).map { sourceIndex ->
                                val source = sources.getJSONObject(sourceIndex)
                                LessonSource(source.getString("title"), source.getString("url"))
                            }
                        }.orEmpty())
                }, item.optJSONObject("titles")?.let { titles -> titles.keys().asSequence().associateWith { titles.getString(it) } }.orEmpty())
        }
        require(lessons.map { it.id }.distinct().size == lessons.size)
        return lessons
    }
}
