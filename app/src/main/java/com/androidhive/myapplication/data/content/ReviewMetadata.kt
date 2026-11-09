package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.ReviewStatus
import com.androidhive.myapplication.core.model.School
import java.util.Properties

/** Separate metadata leaves every instructional source and translation byte-for-byte intact. */
class ReviewMetadata private constructor(private val entries: Map<String, String>) {
    fun status(lessonId: String, school: School, languageTag: String): ReviewStatus {
        val language = AppLanguage.entries.firstOrNull { it.tag == languageTag }
        if (language?.reviewStatus != ReviewStatus.REVIEWED) return ReviewStatus.EXPERIMENTAL
        return ReviewStatus.fromStored(entries["variant.$lessonId.${school.name}.$languageTag"])
    }
    companion object {
        fun parse(text: String): ReviewMetadata {
            val values = Properties().apply { text.reader().use { load(it) } }
            require(values.getProperty("schemaVersion") == "1")
            return ReviewMetadata(values.stringPropertyNames().associateWith { values.getProperty(it) })
        }
    }
}
