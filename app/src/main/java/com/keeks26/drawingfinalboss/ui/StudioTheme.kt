package com.keeks26.drawingfinalboss.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object StudioColors {
    val Paper = Color(0xFFF6F1E7)
    val Ink = Color(0xFF242C29)
    val Rust = Color(0xFFAC452C)
    val Muted = Color(0xFF697064)
    val Line = Color(0xFFDCD5C7)
    val Wash = Color(0xFFEDE6D8)
    val Sage = Color(0xFF5B6B53)
}

@Composable
fun StudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = StudioColors.Rust, onPrimary = StudioColors.Paper,
            primaryContainer = StudioColors.Wash, onPrimaryContainer = StudioColors.Ink,
            secondary = StudioColors.Sage, onSecondary = StudioColors.Paper,
            background = StudioColors.Paper, onBackground = StudioColors.Ink,
            surface = StudioColors.Paper, onSurface = StudioColors.Ink,
            surfaceVariant = StudioColors.Wash, onSurfaceVariant = StudioColors.Muted,
            outline = StudioColors.Muted, outlineVariant = StudioColors.Line,
            error = Color(0xFF9A3027)
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 38.sp, lineHeight = 42.sp),
            headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 34.sp),
            titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 24.sp, lineHeight = 28.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
            bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
            labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
            labelMedium = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp),
            labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp)
        ),
        shapes = Shapes(
            small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
