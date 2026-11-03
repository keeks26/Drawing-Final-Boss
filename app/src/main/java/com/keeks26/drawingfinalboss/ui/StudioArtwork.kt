package com.keeks26.drawingfinalboss.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keeks26.drawingfinalboss.R

@Composable
fun BrandStamp(modifier: Modifier = Modifier) {
    Box(modifier.background(StudioColors.Ink, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null)
    }
}

@Composable
fun StudioWordmark() {
    Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
        BrandStamp(Modifier.size(42.dp))
        Text("DRAWING\nFINAL BOSS", fontSize = 11.sp, lineHeight = 14.sp,
            letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = StudioColors.Ink)
    }
}

enum class StudioSymbol { ARROW, BACK, REFRESH, PLAY, PAUSE, NEXT, CHECK, CLOSE }

@Composable
fun StudioIcon(symbol: StudioSymbol, tint: Color = StudioColors.Ink, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
                drawLine(tint, Offset(x1, y1), Offset(x2, y2), 1.8f, StrokeCap.Round)
            when (symbol) {
                StudioSymbol.ARROW -> { line(4f,12f,20f,12f); line(14f,6f,20f,12f); line(14f,18f,20f,12f) }
                StudioSymbol.BACK -> { line(20f,12f,4f,12f); line(10f,6f,4f,12f); line(10f,18f,4f,12f) }
                StudioSymbol.REFRESH -> {
                    drawArc(tint, 35f, 290f, false, Offset(5f,5f), Size(14f,14f), style = Stroke(1.8f, cap = StrokeCap.Round))
                    line(19f,3f,19f,8f); line(14f,8f,19f,8f)
                }
                StudioSymbol.PLAY -> drawPath(Path().apply { moveTo(8f,5f); lineTo(19f,12f); lineTo(8f,19f); close() }, tint)
                StudioSymbol.PAUSE -> { line(8f,5f,8f,19f); line(16f,5f,16f,19f) }
                StudioSymbol.NEXT -> { line(19f,5f,19f,19f); drawPath(Path().apply { moveTo(5f,5f); lineTo(15f,12f); lineTo(5f,19f); close() },tint) }
                StudioSymbol.CHECK -> { line(5f,12f,10f,17f); line(10f,17f,19f,7f) }
                StudioSymbol.CLOSE -> { line(6f,6f,18f,18f); line(18f,6f,6f,18f) }
            }
        }
    }
}

/** Original vector gesture sketch: decorative artwork, not a reference photo. */
@Composable
fun GestureSketch(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        scale(size.width / 140f, size.height / 210f, pivot = Offset.Zero) {
            drawCircle(StudioColors.Rust.copy(alpha = 0.12f), 58f, Offset(74f,95f))
            drawLine(StudioColors.Line, Offset(8f,194f), Offset(132f,194f), 1f)
            val ink = StudioColors.Ink.copy(alpha = 0.8f)
            drawOval(ink, Offset(62f,14f), Size(22f,29f), style = Stroke(1.6f))
            val contours = listOf(
                Path().apply { moveTo(69f,42f); cubicTo(62f,55f,59f,62f,65f,78f); cubicTo(69f,91f,58f,105f,54f,117f) },
                Path().apply { moveTo(78f,44f); cubicTo(91f,59f,85f,79f,82f,90f); cubicTo(78f,104f,76f,113f,70f,123f) },
                Path().apply { moveTo(65f,51f); cubicTo(48f,59f,40f,70f,31f,85f); lineTo(17f,72f); moveTo(74f,52f); cubicTo(93f,56f,108f,65f,117f,81f); lineTo(125f,99f) },
                Path().apply { moveTo(54f,117f); cubicTo(43f,135f,47f,149f,52f,163f); lineTo(45f,191f); lineTo(34f,195f); moveTo(70f,123f); cubicTo(77f,143f,89f,154f,95f,166f); lineTo(104f,190f); lineTo(117f,194f) },
                Path().apply { moveTo(63f,123f); cubicTo(56f,139f,56f,150f,59f,161f); moveTo(63f,123f); cubicTo(69f,139f,80f,154f,87f,169f) }
            )
            contours.forEach { drawPath(it, ink, style = Stroke(1.6f, cap = StrokeCap.Round)) }
            drawLine(StudioColors.Rust, Offset(49f,94f), Offset(90f,100f), 0.8f)
            drawLine(StudioColors.Rust.copy(alpha = 0.5f), Offset(57f,117f), Offset(80f,115f), 0.8f)
            drawCircle(StudioColors.Rust, 2.2f, Offset(63f,122f))
        }
    }
}
