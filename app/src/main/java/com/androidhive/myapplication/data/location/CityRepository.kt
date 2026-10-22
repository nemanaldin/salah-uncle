package com.androidhive.myapplication.data.location

import android.content.Context
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.LocationMode
import com.androidhive.myapplication.core.model.SavedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import java.util.zip.GZIPInputStream

data class City(val id: String, val country: String, val province: String, val name: String,
    val latitude: Double, val longitude: Double, val zoneId: String,
    val countryCode: String = country, val aliases: List<String> = emptyList()) {
    private val searchKey = normalizeCityQuery(listOf(name, province, country).plus(aliases).joinToString(" "))
    fun matches(normalizedQuery: String) = normalizedQuery.isBlank() || normalizedQuery.split(' ').all { it in searchKey }
    fun countryName(language: AppLanguage): String = countryName(countryCode, language)
    fun location() = SavedLocation(latitude, longitude, name, zoneId, LocationMode.MANUAL, id)
}

fun countryName(code: String, language: AppLanguage): String =
    Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.forLanguageTag(language.tag)).ifBlank { code }

private val queryMarks = Regex("[\\p{M}\\u200c\\u200d]")
fun normalizeCityQuery(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
    .lowercase(Locale.ROOT).replace('ي', 'ی').replace('ك', 'ک')
    .replace(queryMarks, "").trim()

/** A compact offline snapshot. Coordinates/timezones are never guessed from the country. */
class CityRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private val mutex = Mutex()
    private var cached: List<City>? = null
    suspend fun load(): List<City> = withContext(Dispatchers.IO) { mutex.withLock {
        cached ?: buildList {
            val root = JSONObject(assets.open("locations.json").bufferedReader().use { it.readText() })
            val zones = mapOf("ایران" to ("IR" to "Asia/Tehran"), "افغانستان" to ("AF" to "Asia/Kabul"), "تاجیکستان" to ("TJ" to "Asia/Dushanbe"))
            for (country in root.keys()) {
                val provinces = root.getJSONObject(country)
                val (code, zone) = requireNotNull(zones[country])
                for (province in provinces.keys()) {
                    val cities = provinces.getJSONObject(province)
                    for (name in cities.keys()) {
                        val point = cities.getJSONObject(name)
                        add(City("$country/$province/$name", country, province, name,
                            if (point.has("latitude")) point.getDouble("latitude") else point.getDouble("lat"),
                            if (point.has("longitude")) point.getDouble("longitude") else point.getDouble("lon"), zone, code))
                    }
                }
            }
            GZIPInputStream(assets.open("geography/cities-v1.bin")).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val p = line.split('\t', limit = 7)
                    require(p.size == 7)
                    val names = JSONArray(p[6])
                    add(City("geonames:${p[0]}", p[1], "", p[2], p[3].toDouble(), p[4].toDouble(), p[5], p[1],
                        (0 until names.length()).map(names::getString)))
                }
            }
        }.sortedWith(compareBy(City::countryCode, City::name)).also { cities ->
            require(cities.map { it.id }.distinct().size == cities.size)
            cached = cities
        }
    } }
}
