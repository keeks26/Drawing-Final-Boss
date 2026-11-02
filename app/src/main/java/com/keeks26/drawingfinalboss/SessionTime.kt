package com.keeks26.drawingfinalboss

import java.util.Locale

internal fun remainingAfterTick(remainingMillis: Long, elapsedMillis: Long): Long =
    (remainingMillis - elapsedMillis.coerceAtLeast(0)).coerceAtLeast(0)

internal fun formatDuration(seconds: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
