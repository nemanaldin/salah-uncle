package com.androidhive.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.androidhive.myapplication.*
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.AlertMode
import com.androidhive.myapplication.core.model.School
import com.androidhive.myapplication.data.content.Lesson
import com.androidhive.myapplication.platform.openExternal
import com.androidhive.myapplication.platform.openUrl
import com.androidhive.myapplication.platform.shareText
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.home.HomeScreen
import com.androidhive.myapplication.ui.lessons.*
import com.androidhive.myapplication.ui.onboarding.OnboardingScreen
import com.androidhive.myapplication.ui.prayer.PrayerTimesScreen
import com.androidhive.myapplication.ui.qibla.QiblaScreen
import com.androidhive.myapplication.ui.settings.SettingsScreen

data class DeviceActions(val location: () -> Unit, val notifications: () -> Unit, val exactAlarms: () -> Unit, val fullScreen: () -> Unit)
private data class Destination(val route: String, val title: Int, val icon: ImageVector)
private val destinations = listOf(
    Destination("home", R.string.ui_home, Icons.Default.Home),
    Destination("lessons", R.string.ui_lessons, Icons.AutoMirrored.Filled.List),
    Destination("tools", R.string.ui_tools, Icons.Default.DateRange),
    Destination("settings", R.string.ui_settings, Icons.Default.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahApp(state: SalahUiState, model: SalahViewModel, container: AppContainer, actions: DeviceActions,
    pendingRoute: String?, onRouteHandled: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(model) { model.events.collect { snackbar.showSnackbar(context.getString(it)) } }
    val settings = state.settings
    if (settings == null) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.Center) {
                if (state.error != null) Text(stringResource(state.error)) else CircularProgressIndicator()
            }
        }
        return
    }
    if (!settings.onboardingComplete) {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            Box(Modifier.padding(padding)) {
                OnboardingScreen(settings, { language -> model.update { it.copy(language = language) } },
                    { school -> model.update { it.withSchool(school) } },
                    { model.completeOnboarding(settings.language, settings.school) })
            }
        }
        return
    }
    val navigation = rememberNavController()
    val backStack by navigation.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    LaunchedEffect(route) { model.showCountdown(route == "home" || route == "times") }
    val topLevel = destinations.any { it.route == route }
    val selectedRoute = when {
        route.startsWith("lesson/") || route == "training" -> "lessons"
        route == "times" || route == "qibla" -> "tools"
        route == "about" -> "settings"
        else -> route
    }
    val navigate: (String) -> Unit = { destination -> navigation.navigate(destination) { launchSingleTop = true } }
    val selectMain: (String) -> Unit = { destination ->
        navigation.navigate(destination) {
            popUpTo(navigation.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true; restoreState = true
        }
    }
    val lessonId = backStack?.arguments?.getString("id")
    val title = when (route) {
        "times" -> stringResource(R.string.ui_prayer_times)
        "qibla" -> stringResource(R.string.ui_qibla)
        "training" -> stringResource(R.string.ui_training)
        "about" -> stringResource(R.string.ui_about)
        "lesson/{id}" -> state.content.lessons.firstOrNull { it.id == lessonId }?.title(settings.language).orEmpty()
        "prayer-types" -> stringResource(R.string.ui_prayer_types)
        "voices" -> stringResource(R.string.ui_adhan_voice)
        else -> stringResource(destinations.firstOrNull { it.route == route }?.title ?: R.string.ui_app_name)
    }
    LaunchedEffect(pendingRoute) {
        if (pendingRoute in listOf("times", "home")) { navigate(pendingRoute!!); onRouteHandled() }
    }
    val wide = LocalConfiguration.current.screenWidthDp >= 720
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (route == "home") stringResource(R.string.ui_app_name) else title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = { if (!topLevel) IconButton({ navigation.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ui_back)) } },
            actions = { if (topLevel) {
                Text(stringResource(settings.school.labelRes()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (route != "settings") IconButton({ selectMain("settings") }, Modifier.testTag("header_settings")) { Icon(Icons.Default.Settings, stringResource(R.string.ui_settings)) }
            } }) },
        bottomBar = { if (!wide && topLevel) NavigationBar {
            destinations.forEach { destination ->
                NavigationBarItem(selectedRoute == destination.route, { selectMain(destination.route) },
                    icon = { Icon(destination.icon, null) }, label = { Text(stringResource(destination.title)) },
                    modifier = Modifier.testTag("nav_${destination.route}"))
            }
        } }, snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            if (wide) NavigationRail {
                destinations.forEach { destination ->
                    NavigationRailItem(selectedRoute == destination.route, { selectMain(destination.route) },
                        icon = { Icon(destination.icon, null) }, label = { Text(stringResource(destination.title)) },
                        modifier = Modifier.testTag("nav_${destination.route}"))
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                NavHost(navigation, startDestination = "home", modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth().fillMaxHeight(),
                    enterTransition = { androidx.compose.animation.EnterTransition.None }, exitTransition = { androidx.compose.animation.ExitTransition.None }) {
                    composable("home") { HomeScreen(state, { navigate("times") }, { navigate("lesson/$it") }, { selectMain("lessons") }, { navigate("training") }, { navigate("qibla") }, { navigate("prayer-types") }) }
                    composable("lessons") { LessonsScreen(state, { navigate("lesson/$it") }, model::bookmark, { navigate("training") }, model::loadContent) }
                    composable("tools") { ToolsScreen(settings.school, navigate) }
                    composable("prayer-types") { LessonsScreen(state, { navigate("lesson/$it") }, model::bookmark, { navigate("training") }, model::loadContent, "prayers") }
                    composable("voices") { com.androidhive.myapplication.ui.settings.AdhanVoicesScreen(state, model::update, model::importAdhanVoice, model::removeAdhanVoice, model::message) }
                    composable("settings") { SettingsScreen(state, model::update, { mode ->
                        model.update { it.copy(alertMode = mode) }
                        if (mode != AlertMode.OFF) actions.notifications()
                    }, actions.notifications, actions.exactAlarms, actions.fullScreen, { navigate("about") }, model::message, { navigate("voices") }) }
                    composable("times") { PrayerTimesScreen(state, model::selectCity, actions.location, { selectMain("settings") }) }
                    composable("qibla") { QiblaScreen(settings, container.headings, { navigate("times") }) }
                    composable("training") { TrainingScreen(state.content.videos, settings, { navigate("lesson/${settings.school.guideId}") }, model::message) }
                    composable("lesson/{id}") { entry ->
                        val lesson = state.content.lessons.firstOrNull { it.id == entry.arguments?.getString("id") }
                        if (lesson != null) LessonScreen(lesson, settings, model::recordReading, model::completeStep,
                            { navigation.popBackStack() }, model::message)
                        else PageColumn {
                            if (state.content.error == null) CircularProgressIndicator()
                            else {
                                InfoCard(stringResource(state.content.error))
                                TextButton(model::loadContent) { Text(stringResource(R.string.ui_retry)) }
                            }
                        }
                    }
                    composable("about") { AboutScreen(state.content.lessons, settings.school, model::message) }
                }
            }
        }
    }
}

@Composable
private fun ToolsScreen(school: School, navigate: (String) -> Unit) {
    PageColumn {
        val guide = if (school == School.HANAFI) "lesson/dhikr" to R.string.ui_remembrance
            else "lesson/${school.guideId}" to R.string.ui_open_prayer_guide
        listOf("times" to R.string.ui_prayer_times, "qibla" to R.string.ui_qibla, guide, "training" to R.string.ui_training).forEach { (route, label) ->
            AppCard(modifier = Modifier.testTag("tool_${route.replace('/', '_')}"), onClick = { navigate(route) }) { SectionTitle(stringResource(label)) }
        }
    }
}

@Composable
private fun AboutScreen(lessons: List<Lesson>, school: School, onError: (Int) -> Unit) {
    val context = LocalContext.current
    val error = { onError(R.string.ui_error_external_app) }
    val shareLabel = stringResource(R.string.ui_share_app)
    PageColumn {
        AppCard {
            SectionTitle(stringResource(R.string.ui_app_name))
            SupportingText(stringResource(R.string.ui_about_body))
            SupportingText(stringResource(R.string.ui_version, BuildConfig.VERSION_NAME))
        }
        val sources = lessons.mapNotNull { it.variant(school) }.flatMap { it.sources }.distinctBy { it.url }
        sources.forEach { source ->
            OutlinedButton({ openUrl(context, source.url, error) }, Modifier.fillMaxWidth()) { Text(source.title) }
        }
        SupportingText(stringResource(R.string.ui_geo_credit))
        OutlinedButton({ openUrl(context, "https://www.geonames.org/export/", error) }, Modifier.fillMaxWidth()) { Text("GeoNames · CC BY 4.0") }
        OutlinedButton({ openExternal(context, android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:moeinarazi53@gmail.com")), error) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_contact)) }
        OutlinedButton({ openUrl(context, "https://cafebazaar.ir/app/${context.packageName}", error) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_rate)) }
        OutlinedButton({ shareText(context, "https://cafebazaar.ir/app/${context.packageName}", shareLabel, error) }, Modifier.fillMaxWidth()) { Text(shareLabel) }
    }
}
