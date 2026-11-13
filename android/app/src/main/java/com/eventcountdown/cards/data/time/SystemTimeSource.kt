package com.eventcountdown.cards.data.time

import com.eventcountdown.cards.domain.time.TimeSource
import java.time.Instant
import java.time.ZoneId

class SystemTimeSource : TimeSource {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
