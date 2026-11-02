package com.keeks26.drawingfinalboss

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.keeks26.drawingfinalboss.data.DrawingLevel
import com.keeks26.drawingfinalboss.data.FileCatalogCache
import com.keeks26.drawingfinalboss.data.SupabaseCatalog
import com.keeks26.drawingfinalboss.data.publicPhotoUrl
import java.io.File
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

enum class SessionDuration(val label: String, val seconds: Int?) {
    THIRTY_SECONDS("30 sec", 30),
    ONE_MINUTE("60 sec", 60),
    TWO_MINUTES("2 min", 120),
    FIVE_MINUTES("5 min", 300),
    CUSTOM("Custom", null)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawingFinalBossApp(catalogModel)
                }
            }
        }
    }
}

@Composable
private fun DrawingFinalBossApp(model: CatalogViewModel) {
    val catalog by model.state.collectAsStateWithLifecycle()
    var selectedDuration by rememberSaveable { mutableStateOf(SessionDuration.ONE_MINUTE) }
    var customDurationSeconds by rememberSaveable { mutableStateOf(600) }
    var sessionStarted by rememberSaveable { mutableStateOf(false) }
    var showCustomDurationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = sessionStarted) {
        sessionStarted = false
    }

    if (sessionStarted) {
        SessionScreen(
            catalog = catalog,
            durationSeconds = selectedDuration.seconds ?: customDurationSeconds,
            onLoadMore = model::loadMore,
            onRetry = model::refresh,
            onEndSession = { sessionStarted = false }
        )
    } else {
        HomeScreen(
            catalog = catalog,
            onRefresh = model::refresh,
            selectedDuration = selectedDuration,
            customDurationSeconds = customDurationSeconds,
            onLevelSelected = model::selectLevel,
            onDurationSelected = { duration ->
                if (duration == SessionDuration.CUSTOM) {
                    showCustomDurationDialog = true
                } else {
                    selectedDuration = duration
                }
            },
            onStartSession = { sessionStarted = true }
        )
    }

    if (showCustomDurationDialog) {
        CustomDurationDialog(
            initialDurationSeconds = customDurationSeconds,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { seconds ->
                customDurationSeconds = seconds
                selectedDuration = SessionDuration.CUSTOM
                showCustomDurationDialog = false
            }
        )
    }
}

@Composable
private fun HomeScreen(
    catalog: CatalogState,
    onRefresh: () -> Unit,
    selectedDuration: SessionDuration,
    customDurationSeconds: Int,
    onLevelSelected: (DrawingLevel) -> Unit,
    onDurationSelected: (SessionDuration) -> Unit,
    onStartSession: () -> Unit
) {
    val customLabel = if (selectedDuration == SessionDuration.CUSTOM) {
        "Custom · ${formatDuration(customDurationSeconds)}"
    } else {
        "Custom"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Drawing Final Boss",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Train your eye. Draw with intent.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Reference photos for focused drawing practice",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            Text(
                text = "Choose a challenge",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(DrawingLevel.entries) { level ->
            LevelCard(
                level = level,
                selected = level == catalog.level,
                onClick = { onLevelSelected(level) }
            )
        }

        item {
            Text(
                text = "Session duration",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            DurationRow(
                durations = SessionDuration.entries.filter { it != SessionDuration.CUSTOM }.take(2),
                selectedDuration = selectedDuration,
                onDurationSelected = onDurationSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
            DurationRow(
                durations = SessionDuration.entries.filter { it != SessionDuration.CUSTOM }.drop(2),
                selectedDuration = selectedDuration,
                onDurationSelected = onDurationSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
            DurationButton(
                label = customLabel,
                selected = selectedDuration == SessionDuration.CUSTOM,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onDurationSelected(SessionDuration.CUSTOM) }
            )
        }

        item {
            CatalogNotice(catalog, onRefresh)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !catalog.loading && catalog.photos.isNotEmpty(),
                onClick = onStartSession
            ) {
                Text("Start session", fontSize = 16.sp)
            }
            Text(
                text = "The timer starts when your reference photo is ready.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun LevelCard(
    level: DrawingLevel,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = level.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = level.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (selected) {
                Text(
                    text = "Selected",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DurationRow(
    durations: List<SessionDuration>,
    selectedDuration: SessionDuration,
    onDurationSelected: (SessionDuration) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        durations.forEach { duration ->
            DurationButton(
                label = duration.label,
                selected = duration == selectedDuration,
                modifier = Modifier.weight(1f),
                onClick = { onDurationSelected(duration) }
            )
        }
    }
}

@Composable
private fun DurationButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(modifier = modifier, onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(modifier = modifier, onClick = onClick) {
            Text(label)
        }
    }
}

@Composable
private fun CustomDurationDialog(
    initialDurationSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var minutesText by remember(initialDurationSeconds) {
        mutableStateOf((initialDurationSeconds / 60).toString())
    }
    var secondsText by remember(initialDurationSeconds) {
        mutableStateOf((initialDurationSeconds % 60).toString())
    }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom duration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Choose any duration up to 60 minutes.")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter(Char::isDigit).take(4) },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (showError) {
                    Text(
                        text = "Enter a duration from 1 second to 60 minutes.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val minutes = minutesText.toIntOrNull() ?: 0
                val seconds = secondsText.toIntOrNull() ?: 0
                val totalSeconds = minutes * 60 + seconds
                if (seconds in 0..59 && totalSeconds in 1..3600) {
                    onConfirm(totalSeconds)
                } else {
                    showError = true
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CatalogNotice(catalog: CatalogState, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            catalog.loading -> {
                CircularProgressIndicator()
                Text("Loading references…")
            }
            catalog.photos.isEmpty() -> {
                Text(catalog.error ?: "No references yet for this level. Choose another level or check again later.")
                OutlinedButton(onClick = onRetry) { Text("Try again") }
            }
            else -> {
                Text(if (catalog.fromCache) {
                    "Using saved references. Photos you have already viewed may be available offline."
                } else {
                    "${catalog.photos.size}${if (catalog.nextCursor != null) "+" else ""} references ready"
                })
                OutlinedButton(onClick = onRetry) { Text("Refresh references") }
            }
        }
    }
}

@Composable
private fun SessionScreen(
    catalog: CatalogState,
    durationSeconds: Int,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onEndSession: () -> Unit
) {
    if (catalog.photos.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CatalogNotice(catalog, onRetry)
            OutlinedButton(onClick = onEndSession) { Text("Back to home") }
        }
        return
    }

    // Stable IDs retain the selected photo when another page is appended or the screen rotates.
    var photoId by rememberSaveable(catalog.level) { mutableStateOf(catalog.photos.first().id) }
    val photo = catalog.photos.firstOrNull { it.id == photoId } ?: catalog.photos.first()
    val photoIndex = catalog.photos.indexOf(photo)
    var remainingMillis by rememberSaveable(photo.id, durationSeconds) {
        mutableLongStateOf(durationSeconds * 1000L)
    }
    var isPaused by rememberSaveable(photo.id) { mutableStateOf(false) }
    var imageLoaded by remember(photo.id) { mutableStateOf(false) }
    var imageFailed by remember(photo.id) { mutableStateOf(false) }
    var imageAttempt by remember(photo.id) { mutableStateOf(0) }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val foreground = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val secondsLeft = ((remainingMillis + 999) / 1000).toInt()

    LaunchedEffect(photo.id, isPaused, imageLoaded, foreground, remainingMillis > 0) {
        if (imageLoaded && !isPaused && foreground && remainingMillis > 0) {
            var previous = SystemClock.elapsedRealtime()
            while (remainingMillis > 0) {
                delay(100)
                val now = SystemClock.elapsedRealtime()
                remainingMillis = remainingAfterTick(remainingMillis, now - previous)
                previous = now
            }
        }
    }
    LaunchedEffect(photo.id, catalog.photos.size, catalog.nextCursor, catalog.loadingMore, catalog.error) {
        if (photoIndex >= catalog.photos.size - 3 && catalog.nextCursor != null &&
            !catalog.loadingMore && catalog.error == null) onLoadMore()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(catalog.level.title, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Reference ${photoIndex + 1} of ${catalog.photos.size}${if (catalog.nextCursor != null) "+" else ""}",
                    style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onEndSession) { Text("End") }
        }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            key(photo.id, imageAttempt) {
                AsyncImage(
                    model = publicPhotoUrl(BuildConfig.SUPABASE_URL, photo.storagePath),
                    contentDescription = photo.description,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                    onSuccess = { imageFailed = false; imageLoaded = true },
                    onError = { imageLoaded = false; imageFailed = true }
                )
            }
            if (!imageLoaded && !imageFailed) CircularProgressIndicator()
            if (imageFailed) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Photo unavailable. Retry or move to the next pose.", textAlign = TextAlign.Center)
                        TextButton(onClick = { imageFailed = false; imageAttempt++ }) { Text("Retry photo") }
                    }
                }
            }
            if (remainingMillis == 0L) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Time's up", color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (photo.caption.isNotBlank()) {
            Text(photo.caption, style = MaterialTheme.typography.labelSmall,
                maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        if (catalog.fromCache) Text("Using saved references", style = MaterialTheme.typography.labelSmall)
        if (catalog.error != null) {
            Text(catalog.error, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onLoadMore) { Text("Retry loading more") }
        } else if (catalog.loadingMore) {
            Text("Loading more references…", style = MaterialTheme.typography.bodySmall)
        }
        Text(formatDuration(secondsLeft), Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f), enabled = imageLoaded,
                onClick = {
                    if (remainingMillis == 0L) { remainingMillis = durationSeconds * 1000L; isPaused = false }
                    else isPaused = !isPaused
                }
            ) { Text(if (remainingMillis == 0L) "Restart" else if (isPaused) "Resume" else "Pause") }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !(catalog.loadingMore && photoIndex == catalog.photos.lastIndex),
                onClick = {
                    if (photoIndex == catalog.photos.lastIndex && catalog.nextCursor != null) onLoadMore()
                    else {
                        photoId = catalog.photos[(photoIndex + 1) % catalog.photos.size].id
                        // Also reset when a level contains only one reference.
                        remainingMillis = durationSeconds * 1000L
                        isPaused = false
                    }
                }
            ) {
                Text(if (photoIndex == catalog.photos.lastIndex && catalog.nextCursor != null)
                    "Load more poses" else "Next pose")
            }
        }
    }
}
