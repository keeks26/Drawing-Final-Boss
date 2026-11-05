package com.keeks26.drawingfinalboss.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OpeningScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val opacity by animateFloatAsState(if (visible) 1f else 0f, tween(650), label = "opening")
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        visible = true
        // Reference requests run in parallel; a slow connection never traps the user here.
        delay(1450)
        finish()
    }
    Box(Modifier.fillMaxSize().padding(28.dp)) {
        Column(Modifier.align(Alignment.Center).graphicsLayer {
            alpha = opacity
            translationY = (1f - opacity) * 18f
        }, horizontalAlignment = Alignment.CenterHorizontally) {
            BrandStamp(Modifier.size(88.dp))
            Spacer(Modifier.height(32.dp))
            Text("Drawing\nFinal Boss.", fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic, fontSize = 52.sp, lineHeight = 55.sp,
                textAlign = TextAlign.Center, color = StudioColors.Ink)
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(Modifier.width(54.dp), color = StudioColors.Rust, thickness = 2.dp)
            Spacer(Modifier.height(20.dp))
            Text("CREATE A LITTLE EVERYDAY", style = MaterialTheme.typography.labelMedium,
                color = StudioColors.Muted, textAlign = TextAlign.Center)
        }
        Text("YOUR PERSONAL DRAWING STUDIO", Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            style = MaterialTheme.typography.labelMedium, color = StudioColors.Muted, textAlign = TextAlign.Center)
    }
}
