package com.androidhive.myapplication.ui.prayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.home.countdown
import com.androidhive.myapplication.ui.home.formatPrayerTime

@Composable
fun PrayerTimesScreen(state: SalahUiState, onCity: (String) -> Unit, onGps: () -> Unit, onSettings: () -> Unit) {
    val settings = state.settings ?: return
    var showCities by rememberSaveable { mutableStateOf(false) }
    PageColumn(Modifier.testTag("prayer_times_screen")) {
        SupportingText(stringResource(R.string.ui_times_body))
        AppCard {
            SectionTitle(stringResource(R.string.ui_location))
            val location = settings.location
            if (location != null) {
                Text(location.label.ifBlank { stringResource(R.string.ui_device_location) }, style = MaterialTheme.typography.titleMedium)
                SupportingText(stringResource(R.string.ui_timezone, location.zoneId))
            } else SupportingText(stringResource(R.string.ui_location_needed))
            Button({ showCities = true }, Modifier.fillMaxWidth().testTag("choose_city")) { Text(stringResource(R.string.ui_location_manual)) }
            OutlinedButton(onGps, Modifier.fillMaxWidth().testTag("use_gps"), enabled = !state.locationBusy) {
                Text(stringResource(if (state.locationBusy) R.string.ui_location_busy else R.string.ui_location_gps))
            }
        }
        state.next?.let { next -> AppCard {
            SectionTitle(stringResource(R.string.ui_next_prayer))
            Text("${stringResource(next.id.labelRes())} · ${formatPrayerTime(next.time, settings)}", style = MaterialTheme.typography.headlineMedium)
            Text(countdown(state.now, next.time), style = MaterialTheme.typography.headlineMedium)
        } }
        state.today?.let { today -> AppCard {
            SectionTitle(stringResource(R.string.ui_prayer_times))
            today.moments.forEach { moment ->
                val active = moment.id == state.next?.id && moment.time == state.next.time
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(moment.id.labelRes()), color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    Text(formatPrayerTime(moment.time, settings), style = MaterialTheme.typography.titleMedium)
                }
            }
        } }
        state.error?.let { InfoCard(stringResource(it)) }
        OutlinedButton(onSettings, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.ui_notifications)) }
    }
    if (showCities) CityPicker(state.content.cities, settings.language, onChoose = { onCity(it); showCities = false }, onDismiss = { showCities = false })
}

@Composable
private fun CityPicker(cities: List<City>, language: AppLanguage, onChoose: (String) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf<String?>(null) }
    var choosingCountry by rememberSaveable { mutableStateOf(false) }
    var countryQuery by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<City>()) }
    val countries = remember(cities, language) { cities.map { it.countryCode }.distinct().sortedBy { countryName(it, language) } }
    LaunchedEffect(cities, query, country) {
        delay(120)
        results = withContext(Dispatchers.Default) {
            val key = normalizeCityQuery(query)
            cities.asSequence().filter { (country == null || it.countryCode == country) && it.matches(key) }.take(100).toList()
        }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.ui_location_manual)) },
        text = {
            val keyboard = LocalSoftwareKeyboardController.current
            Column(Modifier.fillMaxWidth()) {
                ChoiceRow(stringResource(R.string.ui_country), country?.let { countryName(it, language) } ?: stringResource(R.string.ui_all_countries),
                    { choosingCountry = true; countryQuery = "" }, Modifier.testTag("choose_country"))
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("city_search"), label = { Text(stringResource(R.string.ui_location_search)) }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }))
                LazyColumn(Modifier.heightIn(max = 340.dp).testTag("city_results")) {
                    if (results.isEmpty()) item { SupportingText(stringResource(R.string.ui_location_empty)) }
                    items(results, key = { it.id }) { city ->
                        ListItem(headlineContent = { Text(city.name) }, supportingContent = { Text(listOf(city.countryName(language), city.province, city.zoneId).filter { it.isNotBlank() }.joinToString(" · ")) },
                            modifier = Modifier.clickable { onChoose(city.id) }.testTag("city_${city.id}"))
                    }
                    if (results.size == 100) item { SupportingText(stringResource(R.string.ui_city_refine)) }
                }
                SupportingText(stringResource(R.string.ui_geo_credit))
            }
        }, confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.ui_cancel)) } })
    if (choosingCountry) AlertDialog(onDismissRequest = { choosingCountry = false }, title = { Text(stringResource(R.string.ui_country)) },
        text = {
            val keyboard = LocalSoftwareKeyboardController.current
            Column {
            OutlinedTextField(countryQuery, { countryQuery = it }, Modifier.fillMaxWidth().testTag("country_search"), label = { Text(stringResource(R.string.ui_country_search)) }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }))
            LazyColumn(Modifier.heightIn(max = 350.dp)) {
                item { TextButton({ country = null; choosingCountry = false; query = "" }) { Text(stringResource(R.string.ui_all_countries)) } }
                items(countries.filter { normalizeCityQuery(countryName(it, language)).contains(normalizeCityQuery(countryQuery)) || it.equals(countryQuery, true) }, key = { it }) { code ->
                    ListItem(headlineContent = { Text(countryName(code, language)) }, modifier = Modifier.clickable { country = code; query = ""; choosingCountry = false }.testTag("country_$code"))
                }
            }
        } }, confirmButton = { TextButton({ choosingCountry = false }) { Text(stringResource(R.string.ui_close)) } })
}
