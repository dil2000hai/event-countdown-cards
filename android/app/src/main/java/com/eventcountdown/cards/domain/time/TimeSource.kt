package com.eventcountdown.cards.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface TimeSource {
    fun now(): Instant
    fun zone(): ZoneId
    fun today(): LocalDate = now().atZone(zone()).toLocalDate()
}
