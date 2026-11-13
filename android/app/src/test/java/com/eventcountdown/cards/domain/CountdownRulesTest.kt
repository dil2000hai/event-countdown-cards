package com.eventcountdown.cards.domain

import com.eventcountdown.cards.data.sample.SampleCopy
import com.eventcountdown.cards.data.sample.SampleData
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.time.TimeSource
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.domain.usecase.ValidateEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class CountdownRulesTest {
    private class Clock(var instant: Instant, var timeZone: ZoneId) : TimeSource {
        override fun now() = instant
        override fun zone() = timeZone
    }
    private fun event(date: String) = Event("a", "Celebration", Category.Other, LocalDate.parse(date), Picture.Garden, "", false, 0, 0)
    private fun clock(instant: String, zone: String = "Europe/Berlin") = Clock(Instant.parse(instant), ZoneId.of(zone))

    @Test fun todayTomorrowYesterdayUseCalendarDates() {
        val calculate = CalculateCountdown(clock("2026-11-13T15:00:00Z"))
        assertEquals(0L, calculate(event("2026-11-13")).calendarDays)
        assertEquals(1L, calculate(event("2026-11-14")).calendarDays)
        assertEquals(-1L, calculate(event("2026-11-12")).calendarDays)
        assertEquals(0L, calculate(event("2026-11-13")).wholeHoursUntilStart)
        assertEquals(0L, calculate(event("2026-11-12")).remainingMinutes)
    }
    @Test fun yearBoundaryAndLeapDayAreCalendarDays() {
        assertEquals(1L, CalculateCountdown(clock("2026-12-31T12:00:00Z"))(event("2027-01-01")).calendarDays)
        val leap = CalculateCountdown(clock("2028-02-28T12:00:00Z"))
        assertEquals(1L, leap(event("2028-02-29")).calendarDays)
        assertEquals(2L, leap(event("2028-03-01")).calendarDays)
    }
    @Test fun daylightSavingKeepsDayCountButChangesElapsedHours() {
        val spring = CalculateCountdown(clock("2027-03-27T23:00:00Z"))(event("2027-03-29"))
        assertEquals(1L, spring.calendarDays)
        assertEquals(23L, spring.wholeHoursUntilStart)
        val autumn = CalculateCountdown(clock("2027-10-30T22:00:00Z"))(event("2027-11-01"))
        assertEquals(1L, autumn.calendarDays)
        assertEquals(25L, autumn.wholeHoursUntilStart)
    }
    @Test fun timeZoneIsReadOnEveryCalculation() {
        val time = clock("2026-11-13T00:30:00Z", "UTC")
        val calculate = CalculateCountdown(time)
        assertEquals(0L, calculate(event("2026-11-13")).calendarDays)
        time.timeZone = ZoneId.of("America/Los_Angeles")
        assertEquals(1L, calculate(event("2026-11-13")).calendarDays)
    }
    @Test fun validationHandlesBoundsAndUnicodeCodePoints() {
        val today = LocalDate.of(2026, 11, 13)
        val draft = EventDraft("a", DraftMode.Create, null, "Celebration", Category.Other, today, Picture.Garden)
        val validate = ValidateEvent()
        assertTrue(validate(draft, today).valid)
        assertFalse(validate(draft.copy(name = "   "), today).valid)
        assertFalse(validate(draft.copy(eventDate = today.minusDays(1)), today).valid)
        assertTrue(validate(draft.copy(eventDate = today.plusYears(10)), today).valid)
        assertFalse(validate(draft.copy(eventDate = today.plusYears(10).plusDays(1)), today).valid)
        val symbol = String(Character.toChars(0x1F389))
        assertTrue(validate(draft.copy(name = symbol.repeat(60)), today).valid)
        assertFalse(validate(draft.copy(name = symbol.repeat(61)), today).valid)
        assertFalse(validate(draft.copy(notes = "a".repeat(281)), today).valid)
    }
    @Test fun sampleDatesAreRelativeAndStableWhenPersisted() {
        val copy = SampleCopy(listOf("Lantern evening", "Maya birthday", "Winter holiday", "Mountain weekend", "Garden gathering"), List(5) { "A day worth remembering" })
        val events = SampleData.create(LocalDate.of(2026, 11, 13), 100, copy)
        assertEquals(listOf("2026-11-22", "2026-12-04", "2026-12-25", "2026-12-28", "2026-11-06"), events.map { it.eventDate.toString() })
        assertEquals(5, events.map { it.id }.distinct().size)
        assertTrue(events.first().pinnedInApp)
    }
}
