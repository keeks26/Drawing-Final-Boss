package com.keeks26.drawingfinalboss

import android.os.Bundle
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

data class ReferencePhoto(
    val url: String,
    val gender: ReferenceGender,
    val credit: String = "Unsplash · curated full-body reference",
    val isFullBody: Boolean = true
)

enum class ReferenceGender(val label: String) {
    WOMAN("Woman"),
    MAN("Man")
}

private fun unsplashUrl(photoId: String): String =
    "https://images.unsplash.com/$photoId?auto=format&fit=max&w=1400&q=85"

enum class DrawingLevel(
    val title: String,
    val description: String,
    val photos: List<ReferencePhoto>
) {
    LEVEL_ONE(
        title = "Level 1 · Foundations",
        description = "Relaxed, straightforward poses for learning proportion and gesture.",
        photos = listOf(
            ReferencePhoto(
                url = unsplashUrl("photo-1515886657613-9f3515b0c78f"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1539571696357-5a69c17a67c6"),
                gender = ReferenceGender.MAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1490481651871-ab68de25d43d"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1488161628813-04466f872be2"),
                gender = ReferenceGender.MAN
            )
        )
    ),
    LEVEL_TWO(
        title = "Level 2 · Action",
        description = "Movement and weight shifts that make your gesture drawings feel alive.",
        photos = listOf(
            ReferencePhoto(
                url = unsplashUrl("photo-1506126613408-eca07ce68773"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1517836357463-d25dfeac3438"),
                gender = ReferenceGender.MAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1538805060514-97d9cc17730c"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1534438327276-14e5300c3a48"),
                gender = ReferenceGender.MAN
            )
        )
    ),
    FINAL_BOSS(
        title = "Level 3 · Final Boss",
        description = "Foreshortened angles and difficult viewpoints for a serious challenge.",
        photos = listOf(
            ReferencePhoto(
                url = unsplashUrl("photo-1518611012118-696072aa579a"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1581009146145-b5ef050c2e1a"),
                gender = ReferenceGender.MAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1546483875-ad9014c88eba"),
                gender = ReferenceGender.WOMAN
            ),
            ReferencePhoto(
                url = unsplashUrl("photo-1599058917212-d750089bc078"),
                gender = ReferenceGender.MAN
            )
        )
    )
}

private fun validateReferenceCatalog() {
    val allPhotos = DrawingLevel.entries.flatMap { it.photos }
    val women = allPhotos.count { it.gender == ReferenceGender.WOMAN }
    val men = allPhotos.count { it.gender == ReferenceGender.MAN }

    require(women == men) { "Reference catalog must contain equal women and men." }
    require(allPhotos.all { it.isFullBody }) { "Every reference must be marked as full body." }
    DrawingLevel.entries.forEach { level ->
        val levelWomen = level.photos.count { it.gender == ReferenceGender.WOMAN }
        val levelMen = level.photos.count { it.gender == ReferenceGender.MAN }
        require(levelWomen == levelMen) { "${level.title} must be gender balanced." }
    }
}

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

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawingFinalBossApp()
                }
            }
        }
    }
}

@Composable
private fun DrawingFinalBossApp() {
    remember { validateReferenceCatalog() }

    var selectedLevel by rememberSaveable { mutableStateOf(DrawingLevel.LEVEL_ONE) }
    var selectedDuration by rememberSaveable { mutableStateOf(SessionDuration.ONE_MINUTE) }
    var customDurationSeconds by rememberSaveable { mutableStateOf(600) }
    var sessionStarted by rememberSaveable { mutableStateOf(false) }
    var showCustomDurationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = sessionStarted) {
        sessionStarted = false
    }

    if (sessionStarted) {
        SessionScreen(
            level = selectedLevel,
            durationSeconds = selectedDuration.seconds ?: customDurationSeconds,
            onEndSession = { sessionStarted = false }
        )
    } else {
        HomeScreen(
            selectedLevel = selectedLevel,
            selectedDuration = selectedDuration,
            customDurationSeconds = customDurationSeconds,
            onLevelSelected = { selectedLevel = it },
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
    selectedLevel: DrawingLevel,
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
                text = "Full-body references · 50% women · 50% men",
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
                selected = level == selectedLevel,
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartSession
            ) {
                Text("Start session", fontSize = 16.sp)
            }
            Text(
                text = "Photos load from Unsplash when a session starts.",
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
                        onValueChange = { minutesText = it.filter(Char::isDigit) },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it.filter(Char::isDigit) },
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
                if (totalSeconds in 1..3600) {
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
private fun SessionScreen(
    level: DrawingLevel,
    durationSeconds: Int,
    onEndSession: () -> Unit
) {
    var secondsLeft by rememberSaveable(durationSeconds) { mutableStateOf(durationSeconds) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var photoIndex by rememberSaveable { mutableStateOf(0) }
    val photo = level.photos[photoIndex % level.photos.size]
    var imageFailed by remember(photo.url) { mutableStateOf(false) }

    LaunchedEffect(isPaused, secondsLeft) {
        if (!isPaused && secondsLeft > 0) {
            delay(1000L)
            secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reserve room for End before measuring the title, including at
            // large font sizes and on smaller screens.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Reference ${photoIndex + 1} of ${level.photos.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onEndSession) {
                Text("End")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo.url,
                contentDescription = "Real person figure drawing reference",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onError = { imageFailed = true }
            )
            if (imageFailed) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Photo unavailable. Check your internet connection.",
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (secondsLeft == 0) {
                Surface(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Time's up",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = "${photo.gender.label} · Full-body · ${photo.credit}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = formatDuration(secondsLeft),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (secondsLeft == 0) {
                        secondsLeft = durationSeconds
                    } else {
                        isPaused = !isPaused
                    }
                }
            ) {
                Text(if (secondsLeft == 0) "Restart" else if (isPaused) "Resume" else "Pause")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    photoIndex = (photoIndex + 1) % level.photos.size
                    secondsLeft = durationSeconds
                    isPaused = false
                }
            ) {
                Text("Next pose")
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
