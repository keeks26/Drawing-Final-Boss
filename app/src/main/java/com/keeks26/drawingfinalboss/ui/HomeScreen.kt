package com.keeks26.drawingfinalboss.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keeks26.drawingfinalboss.CatalogState
import com.keeks26.drawingfinalboss.SessionDuration
import com.keeks26.drawingfinalboss.data.DrawingLevel
import com.keeks26.drawingfinalboss.formatDuration

internal val DrawingLevel.shortName: String get() = when (this) {
    DrawingLevel.LEVEL_ONE -> "Foundations"
    DrawingLevel.LEVEL_TWO -> "Action"
    DrawingLevel.FINAL_BOSS -> "Final Boss"
}

@Composable
fun HomeScreen(
    catalog: CatalogState,
    onRefresh: () -> Unit,
    selectedDuration: SessionDuration,
    customDurationSeconds: Int,
    onLevelSelected: (DrawingLevel) -> Unit,
    onDurationSelected: (SessionDuration) -> Unit,
    onStartSession: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f), contentPadding = PaddingValues(22.dp, 20.dp, 22.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    StudioWordmark()
                    Text("A DAILY\nPRACTICE", style = MaterialTheme.typography.labelMedium,
                        color = StudioColors.Muted)
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = StudioColors.Line)
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.62f)) {
                        Text("Make your\nnext mark.", style = MaterialTheme.typography.headlineLarge,
                            fontStyle = FontStyle.Italic, letterSpacing = (-1).sp)
                        Spacer(Modifier.height(14.dp))
                        Text("One pose. A few minutes.\nA little better, every day.",
                            style = MaterialTheme.typography.bodyMedium, color = StudioColors.Muted)
                    }
                    GestureSketch(Modifier.weight(0.38f).height(190.dp))
                }
            }
            item { SectionHeading("01", "Choose your challenge") }
            items(DrawingLevel.entries, key = { it.difficulty }) { level ->
                ChallengeCard(level, level == catalog.level) { onLevelSelected(level) }
            }
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeading("02", "Set your rhythm")
                Text("TIME PER POSE", Modifier.padding(top = 10.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelMedium, color = StudioColors.Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SessionDuration.entries.filter { it.seconds != null }.forEach { duration ->
                        DurationChip(
                            formatDuration(requireNotNull(duration.seconds)), duration == selectedDuration,
                            Modifier.weight(1f)
                        ) { onDurationSelected(duration) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                DurationChip(
                    if (selectedDuration == SessionDuration.CUSTOM)
                        "Custom · ${formatDuration(customDurationSeconds)}" else "Choose a custom duration",
                    selectedDuration == SessionDuration.CUSTOM, Modifier.fillMaxWidth()
                ) { onDurationSelected(SessionDuration.CUSTOM) }
            }
        }

        HorizontalDivider(color = StudioColors.Line)
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp)) {
            LibraryStatus(catalog, onRefresh)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onStartSession, enabled = !catalog.loading && catalog.photos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp), shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Start drawing", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("${formatDuration(selectedDuration.seconds ?: customDurationSeconds)} · ${catalog.level.shortName}",
                        fontSize = 11.sp, fontWeight = FontWeight.Normal)
                }
                StudioIcon(StudioSymbol.ARROW, MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun SectionHeading(number: String, title: String) {
    Row(Modifier.padding(top = 2.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(number, style = MaterialTheme.typography.labelMedium, color = StudioColors.Rust)
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ChallengeCard(level: DrawingLevel, isSelected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(if (isSelected) StudioColors.Ink else StudioColors.Paper, label = "challenge")
    val foreground = if (isSelected) StudioColors.Paper else StudioColors.Ink
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth().semantics { selected = isSelected },
        shape = RoundedCornerShape(18.dp), color = background,
        border = BorderStroke(1.dp, if (isSelected) StudioColors.Ink else StudioColors.Line)
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(if (isSelected) StudioColors.Rust else StudioColors.Wash, CircleShape),
                contentAlignment = Alignment.Center) {
                Text("0${level.difficulty}", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = foreground)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(level.shortName, style = MaterialTheme.typography.titleLarge, color = foreground)
                Text(level.description, style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) StudioColors.Paper.copy(alpha = 0.78f) else StudioColors.Muted)
            }
            StudioIcon(if (isSelected) StudioSymbol.CHECK else StudioSymbol.ARROW, foreground, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DurationChip(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = modifier.heightIn(min = 48.dp).semantics { selected = isSelected },
        color = if (isSelected) StudioColors.Ink else StudioColors.Paper, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) StudioColors.Ink else StudioColors.Line)
    ) {
        Box(Modifier.padding(horizontal = 6.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                color = if (isSelected) StudioColors.Paper else StudioColors.Ink)
        }
    }
}

@Composable
private fun LibraryStatus(catalog: CatalogState, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (catalog.loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 1.5.dp)
        else Box(Modifier.size(6.dp).background(
            if (catalog.photos.isEmpty()) StudioColors.Rust else StudioColors.Sage, CircleShape))
        Text(
            when {
                catalog.loading -> "Finding your next reference…"
                catalog.error != null -> catalog.error
                catalog.photos.isEmpty() -> "No references here yet. Try another challenge."
                catalog.fromCache -> "Saved references · available offline if viewed before"
                else -> "${catalog.photos.size}${if (catalog.nextCursor != null) "+" else ""} references ready"
            },
            Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = StudioColors.Muted
        )
        IconButton(onClick = onRefresh, enabled = !catalog.loading,
            modifier = Modifier.semantics { contentDescription = "Refresh references" }) {
            StudioIcon(StudioSymbol.REFRESH, StudioColors.Muted)
        }
    }
}

@Composable
fun CustomDurationDialog(initialDurationSeconds: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var minutes by remember { mutableStateOf((initialDurationSeconds / 60).toString()) }
    var seconds by remember { mutableStateOf((initialDurationSeconds % 60).toString()) }
    var showError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Find your rhythm", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Give each pose a little time. Choose anything from one second to an hour.")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(4); showError = false },
                        label = { Text("Minutes") }, singleLine = true, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = seconds,
                        onValueChange = { seconds = it.filter(Char::isDigit).take(2); showError = false },
                        label = { Text("Seconds") }, singleLine = true, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                if (showError) Text("Use 0–59 seconds and a total between 1 second and 60 minutes.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = minutes.toIntOrNull() ?: 0
                val s = seconds.toIntOrNull() ?: 0
                if (s in 0..59 && m * 60 + s in 1..3600) onConfirm(m * 60 + s) else showError = true
            }) { Text("Set duration") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
