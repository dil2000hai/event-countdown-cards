package com.eventcountdown.cards.data.sample

import com.eventcountdown.cards.domain.model.Category
import com.eventcountdown.cards.domain.model.Event
import com.eventcountdown.cards.domain.model.Picture
import java.time.LocalDate
import java.util.UUID

data class SampleCopy(val names: List<String>, val notes: List<String>)

object SampleData {
    fun create(today: LocalDate, now: Long, copy: SampleCopy): List<Event> {
        val christmas = LocalDate.of(today.year, 12, 25).let { if (it < today) it.plusYears(1) else it }
        val dates = listOf(today.plusDays(9), today.plusDays(21), christmas, today.plusDays(45), today.minusDays(7))
        val categories = listOf(Category.Other, Category.Birthday, Category.Holiday, Category.Trip, Category.Other)
        val pictures = listOf(Picture.Garden, Picture.Birthday, Picture.Winter, Picture.Mountains, Picture.Garden)
        return dates.indices.map { i -> Event(UUID.randomUUID().toString(), copy.names[i], categories[i], dates[i], pictures[i], copy.notes[i], i == 0, now, now) }
    }
}
