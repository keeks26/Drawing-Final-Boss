package com.keeks26.drawingfinalboss

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeTest {
    @Test fun timerAccountsForDelayedTicksAndNeverGoesNegative() {
        assertEquals(8_650L, remainingAfterTick(10_000L, 1_350L))
        assertEquals(0L, remainingAfterTick(100L, 400L))
        assertEquals(10_000L, remainingAfterTick(10_000L, -5L))
    }

    @Test fun durationsUseMinutesAndSeconds() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("01:05", formatDuration(65))
        assertEquals("60:00", formatDuration(3600))
    }
}
