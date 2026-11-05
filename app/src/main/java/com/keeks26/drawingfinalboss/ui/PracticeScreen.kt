package com.keeks26.drawingfinalboss.ui

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import coil.compose.AsyncImage
import com.keeks26.drawingfinalboss.BuildConfig
import com.keeks26.drawingfinalboss.CatalogState
import com.keeks26.drawingfinalboss.data.ReferencePhoto
import com.keeks26.drawingfinalboss.data.publicPhotoUrl
import com.keeks26.drawingfinalboss.formatDuration
import com.keeks26.drawingfinalboss.remainingAfterTick
import kotlinx.coroutines.delay

@Composable
fun PracticeScreen(
    catalog: CatalogState,
    durationSeconds: Int,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onEndSession: () -> Unit
) {
    if (catalog.photos.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("A moment for your next mark.", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            if (catalog.loading) CircularProgressIndicator()
            else {
                Text(catalog.error ?: "No references yet. Choose another challenge.")
                TextButton(onClick = onRetry) { Text("Try again") }
            }
            TextButton(onClick = onEndSession) { Text("Back to studio") }
        }
        return
    }

    var photoId by rememberSaveable(catalog.level) { mutableStateOf(catalog.photos.first().id) }
    val photo = catalog.photos.firstOrNull { it.id == photoId } ?: catalog.photos.first()
    val photoIndex = catalog.photos.indexOf(photo)
    var remainingMillis by rememberSaveable(photo.id, durationSeconds) { mutableLongStateOf(durationSeconds * 1000L) }
    var isPaused by rememberSaveable(photo.id) { mutableStateOf(false) }
    var imageLoaded by remember(photo.id) { mutableStateOf(false) }
    var imageFailed by remember(photo.id) { mutableStateOf(false) }
    var imageAttempt by remember(photo.id) { mutableIntStateOf(0) }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val foreground = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    var showDetails by remember(photo.id) { mutableStateOf(false) }

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

    val onNext: () -> Unit = {
        if (photoIndex == catalog.photos.lastIndex && catalog.nextCursor != null) onLoadMore()
        else {
            photoId = catalog.photos[(photoIndex + 1) % catalog.photos.size].id
            remainingMillis = durationSeconds * 1000L
            isPaused = false
        }
    }
    val onPause: () -> Unit = {
        if (remainingMillis == 0L) { remainingMillis = durationSeconds * 1000L; isPaused = false }
        else isPaused = !isPaused
    }

    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        val landscape = maxWidth > maxHeight
        val controlsMaxHeight = maxHeight * 0.5f
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedIconButton(
                    onClick = onEndSession, border = BorderStroke(1.dp, StudioColors.Line),
                    modifier = Modifier.semantics { contentDescription = "End session and return home" }
                ) { StudioIcon(StudioSymbol.BACK) }
                Column(Modifier.weight(1f)) {
                    Text("IN THE STUDIO", style = MaterialTheme.typography.labelMedium, color = StudioColors.Muted)
                    Text(catalog.level.shortName, style = MaterialTheme.typography.titleLarge, fontStyle = FontStyle.Italic)
                }
                Text("${(photoIndex + 1).toString().padStart(2, '0')} / ${catalog.photos.size}${if (catalog.nextCursor != null) "+" else ""}",
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = StudioColors.Muted)
            }

            val image: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(StudioColors.Paper),
                        contentAlignment = Alignment.Center) {
                        key(photo.id, imageAttempt) {
                            AsyncImage(
                                model = publicPhotoUrl(BuildConfig.SUPABASE_URL, photo.storagePath),
                                contentDescription = photo.description,
                                modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit,
                                onSuccess = { imageFailed = false; imageLoaded = true },
                                onError = { imageLoaded = false; imageFailed = true }
                            )
                        }
                        if (!imageLoaded && !imageFailed) {
                            CircularProgressIndicator(color = StudioColors.Paper, strokeWidth = 2.dp)
                        }
                        if (imageFailed) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("This reference is taking a break.", color = StudioColors.Paper,
                                    style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                                Text("Check your connection, retry, or move to the next pose.",
                                    color = StudioColors.Paper.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                                TextButton(onClick = { imageFailed = false; imageAttempt++ }) {
                                    Text("Retry photo", color = StudioColors.Paper)
                                }
                            }
                        }
                        if (remainingMillis == 0L) {
                            Surface(Modifier.fillMaxSize(), color = StudioColors.Ink.copy(alpha = 0.85f)) {
                                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center) {
                                    Text("A mark well made.", color = StudioColors.Paper,
                                        style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic,
                                        textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(10.dp))
                                    Text("Time’s up. Ready for the next one?", color = StudioColors.Paper.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    if (photo.caption.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(photo.caption, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                                color = StudioColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            TextButton(onClick = { showDetails = true }) { Text("Details", fontSize = 11.sp) }
                        }
                    }
                }
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (catalog.fromCache) Text("Working from saved references", style = MaterialTheme.typography.bodySmall,
                        color = StudioColors.Muted)
                    if (catalog.error != null) {
                        Text(catalog.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onLoadMore) { Text("Retry loading more") }
                    } else if (catalog.loadingMore) {
                        Text("Preparing more poses…", style = MaterialTheme.typography.bodySmall, color = StudioColors.Muted)
                    }
                    TimerCard(remainingMillis, durationSeconds, isPaused, imageLoaded)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onPause, enabled = imageLoaded, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(12.dp)) {
                            StudioIcon(if (remainingMillis == 0L) StudioSymbol.REFRESH else if (isPaused) StudioSymbol.PLAY else StudioSymbol.PAUSE,
                                MaterialTheme.colorScheme.onPrimary, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (remainingMillis == 0L) "Restart" else if (isPaused) "Resume" else "Pause")
                        }
                        OutlinedButton(onClick = onNext, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            enabled = !(catalog.loadingMore && photoIndex == catalog.photos.lastIndex),
                            shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, StudioColors.Line),
                            contentPadding = PaddingValues(12.dp)) {
                            Text(if (photoIndex == catalog.photos.lastIndex && catalog.nextCursor != null) "More poses" else "Next pose",
                                color = StudioColors.Ink)
                            Spacer(Modifier.width(8.dp))
                            StudioIcon(StudioSymbol.NEXT, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            if (landscape) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    image(Modifier.weight(1.3f).fillMaxHeight())
                    controls(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()))
                }
            } else {
                image(Modifier.weight(1f))
                controls(Modifier.heightIn(max = controlsMaxHeight).verticalScroll(rememberScrollState()))
            }
        }
    }
    if (showDetails) PhotoDetails(photo) { showDetails = false }
}

@Composable
private fun TimerCard(remainingMillis: Long, durationSeconds: Int, isPaused: Boolean, ready: Boolean) {
    Surface(color = StudioColors.Ink, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text(when { !ready -> "GETTING READY"; remainingMillis == 0L -> "SESSION COMPLETE";
                    isPaused -> "TAKE A BREATH"; else -> "TIME TO MAKE MARKS" },
                    style = MaterialTheme.typography.labelMedium, color = StudioColors.Paper.copy(alpha = 0.75f))
                Text(formatDuration(((remainingMillis + 999) / 1000).toInt()),
                    fontFamily = FontFamily.Monospace, fontSize = 46.sp, lineHeight = 54.sp,
                    letterSpacing = (-2).sp, fontWeight = FontWeight.Light, color = StudioColors.Paper)
            }
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { (remainingMillis / (durationSeconds * 1000f)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(), color = StudioColors.Paper,
                    trackColor = StudioColors.Paper.copy(alpha = 0.15f), strokeWidth = 2.dp)
                Box(Modifier.size(7.dp).background(StudioColors.Rust, RoundedCornerShape(50)))
            }
        }
    }
}

@Composable
private fun PhotoDetails(photo: ReferencePhoto, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("About this reference", style = MaterialTheme.typography.headlineMedium) },
        text = { Text(photo.caption, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Back to drawing") } })
}
