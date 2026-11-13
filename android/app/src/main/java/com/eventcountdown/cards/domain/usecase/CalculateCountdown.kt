package com.eventcountdown.cards.domain.usecase

import com.eventcountdown.cards.domain.model.Event
import com.eventcountdown.cards.domain.time.TimeSource
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Countdown(
    val calendarDays: Long,
    val wholeHoursUntilStart: Long,
    val remainingMinutes: Long,
    val isArchived: Boolean,
    val today: LocalDate
)

class CalculateCountdown(private val clock: TimeSource) {
    operator fun invoke(event: Event): Countdown {
        val zone = clock.zone()
        val now = clock.now()
        val today = now.atZone(zone).toLocalDate()
        val minutes = Duration.between(now, event.eventDate.atStartOfDay(zone).toInstant()).toMinutes().coerceAtLeast(0)
        return Countdown(ChronoUnit.DAYS.between(today, event.eventDate), minutes / 60, minutes % 60, event.manuallyArchivedAt != null, today)
    }
}
