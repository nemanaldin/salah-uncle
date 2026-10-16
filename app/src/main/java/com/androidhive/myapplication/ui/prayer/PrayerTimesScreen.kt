package com.androidhive.myapplication.ui.prayer

import androidx.compose.foundation.clickable
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
import com.androidhive.myapplication.data.location.City
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
    if (showCities) CityPicker(state.content.cities, onChoose = { onCity(it); showCities = false }, onDismiss = { showCities = false })
}

@Composable
private fun CityPicker(cities: List<City>, onChoose: (String) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(cities, query) { cities.filter { query.isBlank() || it.name.contains(query, true) || it.province.contains(query, true) || it.country.contains(query, true) } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.ui_location_manual)) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("city_search"), label = { Text(stringResource(R.string.ui_location_search)) }, singleLine = true)
                LazyColumn(Modifier.heightIn(max = 380.dp).testTag("city_results")) {
                    if (results.isEmpty()) item { SupportingText(stringResource(R.string.ui_location_empty)) }
                    items(results, key = { it.id }) { city ->
                        ListItem(headlineContent = { Text(city.name) }, supportingContent = { Text(stringResource(R.string.ui_city_location, city.country, city.province)) },
                            modifier = Modifier.clickable { onChoose(city.id) }.testTag("city_${city.id}"))
                    }
                }
            }
        }, confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.ui_cancel)) } })
}
