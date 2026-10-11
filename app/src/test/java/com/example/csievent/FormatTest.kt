package com.example.csievent

import com.example.csievent.presentation.components.EventPhase
import com.example.csievent.presentation.components.eventPhase
import com.example.csievent.presentation.components.formatRelativeDate
import com.example.csievent.presentation.components.formatSchedule
import com.example.csievent.presentation.components.formatTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FormatTest {

    private val today = LocalDate.of(2026, 10, 10)

    @Test
    fun `event phase follows the date until results are out`() {
        assertEquals(EventPhase.UPCOMING, eventPhase("2026-10-11", scoringLocked = false, today = today))
        assertEquals(EventPhase.TODAY, eventPhase("2026-10-10", scoringLocked = false, today = today))
        assertEquals(EventPhase.ENDED, eventPhase("2026-10-09", scoringLocked = false, today = today))
        assertEquals(EventPhase.RESULTS, eventPhase("2026-10-11", scoringLocked = true, today = today))
        assertEquals(EventPhase.UPCOMING, eventPhase(null, scoringLocked = false, today = today))
        assertEquals(EventPhase.UPCOMING, eventPhase("not a date", scoringLocked = false, today = today))
    }

    @Test
    fun `times are shown in 12-hour format`() {
        assertEquals("2:30 PM", formatTime("14:30:00"))
        assertEquals("9:05 AM", formatTime("09:05"))
        assertEquals("12:00 AM", formatTime("00:00:00"))
        assertNull(formatTime(null))
        assertNull(formatTime("later"))
    }

    @Test
    fun `nearby dates read as words`() {
        assertEquals("Today", formatRelativeDate("2026-10-10", today))
        assertEquals("Tomorrow", formatRelativeDate("2026-10-11", today))
        assertEquals("Yesterday", formatRelativeDate("2026-10-09", today))
        assertEquals("Date TBA", formatRelativeDate(null, today))
    }

    @Test
    fun `schedule line skips missing parts`() {
        assertEquals("Today · 10:30 AM · AB-1 Seminar Hall", formatSchedule("2026-10-10", "10:30:00", " AB-1 Seminar Hall ", today))
        assertEquals("Tomorrow", formatSchedule("2026-10-11", null, "  ", today))
    }
}
