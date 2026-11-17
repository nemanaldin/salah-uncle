package com.androidhive.myapplication.data.duas

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class DuaRepository(context: Context) {
    private val assets = context.applicationContext.assets
    suspend fun load(): DuaCatalog = withContext(Dispatchers.IO) {
        val catalog = parseDuaCatalog(assets.open("duas/duas-v1.json").bufferedReader().use { it.readText() })
        val persian = parsePersianDuas(assets.open("duas/duas-fa-v1.json").bufferedReader().use { it.readText() })
        require(persian.keys == catalog.items.map { it.id }.toSet())
        catalog.copy(items = catalog.items.map { it.copy(persian = persian.getValue(it.id)) })
    }
}
internal fun parsePersianDuas(text: String): Map<String, PersianDuaContent> {
    val root = JSONObject(text)
    require(root.getInt("version") == 1 && root.getString("language") == "fa" && root.getString("school") == "hanafi")
    val items = root.getJSONArray("items").objects()
    require(items.map { it.getString("id") }.distinct().size == items.size)
    return items.associate { item ->
        require(item.getString("reviewStatus") == PENDING_OWNER_REVIEW)
        item.getString("id") to PersianDuaContent(item.getString("arabic"), item.getJSONArray("pronunciation").strings(),
            item.getString("meaning"), item.getString("practice"), item.getJSONArray("sources").objects().map {
                DuaSource(it.getString("book"), it.getString("reference"), it.getString("url"),
                    DuaGrade.valueOf(it.getString("grade")), it.getString("grader"), it.getBoolean("checked")) },
            item.getJSONArray("missingFields").strings().toSet(), item.getString("reviewStatus"),
            item.getString("kind"), item.getBoolean("reminder"), item.getString("title"))
    }
}
internal fun parseDuaCatalog(text: String): DuaCatalog {
    val root = JSONObject(text)
    require(root.getInt("version") == 1 && root.getString("school") == "hanafi")
    return DuaCatalog(
        root.getJSONArray("categories").objects().map { DuaCategory(it.getString("id"), it.getJSONObject("titles").stringMap()) },
        root.getJSONArray("sections").objects().map { DuaSection(it.getString("id"), it.getString("category"),
            it.getJSONObject("titles").stringMap(), it.getJSONArray("duaIds").strings()) },
        root.getJSONArray("items").objects().map { item -> DuaItem(
            item.getString("id"), item.getString("category"), item.getString("section"), item.getJSONObject("titles").stringMap(),
            item.getString("arabic"), item.getJSONObject("pronunciations").let { p -> p.keys().asSequence().associateWith { p.getJSONArray(it).strings() } },
            item.getJSONObject("translations").stringMap(), item.getJSONObject("notes").stringMap(),
            item.getJSONArray("sources").objects().map { DuaSource(it.getString("book"), it.getString("reference"),
                it.getString("url"), DuaGrade.valueOf(it.getString("grade")), it.getString("grader"), it.getBoolean("checked")) },
            item.getJSONArray("repeats").objects().map { DuaRepeat(it.getString("arabic"), it.getInt("count")) },
            item.getString("audio"), item.getJSONArray("reviewFields").strings().toSet(), item.getBoolean("ownerText"), item.getBoolean("reminder"), item.optBoolean("guidance", false)) }
    )
}
private fun JSONObject.stringMap() = keys().asSequence().associateWith { getString(it) }.filterValues { it.isNotBlank() }
private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
private fun JSONArray.strings() = (0 until length()).map { getString(it) }
