package com.androidhive.myapplication.data.location

import android.content.Context
import com.androidhive.myapplication.core.model.LocationMode
import com.androidhive.myapplication.core.model.SavedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class City(val id: String, val country: String, val province: String, val name: String, val latitude: Double, val longitude: Double, val zoneId: String) {
    fun location() = SavedLocation(latitude, longitude, name, zoneId, LocationMode.MANUAL, id)
}

class CityRepository(context: Context) {
    private val assets = context.applicationContext.assets
    suspend fun load(): List<City> = withContext(Dispatchers.IO) {
        val root = JSONObject(assets.open("locations.json").bufferedReader().use { it.readText() })
        val zones = mapOf("ایران" to "Asia/Tehran", "افغانستان" to "Asia/Kabul", "تاجیکستان" to "Asia/Dushanbe")
        buildList {
            for (country in root.keys()) {
                val provinces = root.getJSONObject(country)
                val zone = requireNotNull(zones[country]) { "Missing timezone for $country" }
                for (province in provinces.keys()) {
                    val cities = provinces.getJSONObject(province)
                    for (name in cities.keys()) {
                        val point = cities.getJSONObject(name)
                        add(City("$country/$province/$name", country, province, name,
                            if (point.has("latitude")) point.getDouble("latitude") else point.getDouble("lat"),
                            if (point.has("longitude")) point.getDouble("longitude") else point.getDouble("lon"), zone))
                    }
                }
            }
        }.sortedWith(compareBy(City::country, City::province, City::name))
    }
}
