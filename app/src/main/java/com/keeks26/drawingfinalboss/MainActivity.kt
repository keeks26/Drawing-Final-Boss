package com.keeks26.drawingfinalboss

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keeks26.drawingfinalboss.data.FileCatalogCache
import com.keeks26.drawingfinalboss.data.SupabaseCatalog
import com.keeks26.drawingfinalboss.ui.*
import java.io.File

enum class SessionDuration(val label: String, val seconds: Int?) {
    THIRTY_SECONDS("30 sec", 30),
    ONE_MINUTE("60 sec", 60),
    TWO_MINUTES("2 min", 120),
    FIVE_MINUTES("5 min", 300),
    CUSTOM("Custom", null)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val catalogModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel(
                SupabaseCatalog(
                    BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY,
                    FileCatalogCache(File(applicationContext.filesDir, "reference_catalog"))
                )
            ) as T
        })[CatalogViewModel::class.java]

        setContent {
            StudioTheme {
                Surface(Modifier.fillMaxSize().safeDrawingPadding()) {
                    DrawingFinalBossApp(catalogModel)
                }
            }
        }
    }
}

@Composable
private fun DrawingFinalBossApp(model: CatalogViewModel) {
    val catalog by model.state.collectAsStateWithLifecycle()
    var openingComplete by rememberSaveable { mutableStateOf(false) }
    var selectedDuration by rememberSaveable { mutableStateOf(SessionDuration.ONE_MINUTE) }
    var customDurationSeconds by rememberSaveable { mutableStateOf(600) }
    var sessionStarted by rememberSaveable { mutableStateOf(false) }
    var showCustomDurationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = sessionStarted) { sessionStarted = false }
    val screen = when { !openingComplete -> "opening"; sessionStarted -> "session"; else -> "home" }
    Crossfade(targetState = screen, animationSpec = tween(300), label = "studio screen") { target ->
        when (target) {
            "opening" -> OpeningScreen { openingComplete = true }
            "session" -> PracticeScreen(
                catalog = catalog, durationSeconds = selectedDuration.seconds ?: customDurationSeconds,
                onLoadMore = model::loadMore, onRetry = model::refresh,
                onEndSession = { sessionStarted = false }
            )
            else -> HomeScreen(
                catalog = catalog, onRefresh = model::refresh, selectedDuration = selectedDuration,
                customDurationSeconds = customDurationSeconds, onLevelSelected = model::selectLevel,
                onDurationSelected = { duration ->
                    if (duration == SessionDuration.CUSTOM) showCustomDurationDialog = true
                    else selectedDuration = duration
                },
                onStartSession = { sessionStarted = true }
            )
        }
    }
    if (showCustomDurationDialog) {
        CustomDurationDialog(customDurationSeconds, onDismiss = { showCustomDurationDialog = false },
            onConfirm = { seconds ->
                customDurationSeconds = seconds
                selectedDuration = SessionDuration.CUSTOM
                showCustomDurationDialog = false
            })
    }
}
