package com.eventcountdown.cards.core.di

import android.content.Context
import android.provider.Settings
import com.eventcountdown.cards.R
import com.eventcountdown.cards.data.about.AppInfoProvider
import com.eventcountdown.cards.data.local.CountdownPreferences
import com.eventcountdown.cards.data.repository.CountdownRepositoryImpl
import com.eventcountdown.cards.data.sample.SampleCopy
import com.eventcountdown.cards.data.time.SystemTimeSource
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.usecase.CalculateCountdown

object ServiceLocator {
    private var dependencies: Dependencies? = null
    fun initialize(context: Context) {
        if (dependencies != null) return
        val app = context.applicationContext
        val clock = SystemTimeSource()
        val names = listOf(R.string.sample_lantern, R.string.sample_birthday, R.string.sample_winter, R.string.sample_mountains, R.string.sample_garden).map { app.getString(it) }
        val notes = listOf(R.string.note_lantern, R.string.note_birthday, R.string.note_winter, R.string.note_mountains, R.string.note_garden).map { app.getString(it) }
        val repository = CountdownRepositoryImpl(CountdownPreferences(app), clock, SampleCopy(names, notes))
        dependencies = Dependencies(repository, clock, CalculateCountdown(clock), AppInfoProvider(app), app.getString(R.string.default_name)) { Settings.Global.getFloat(app.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }
    }
    fun get(): Dependencies = checkNotNull(dependencies)
    fun refreshClock() { dependencies?.repository?.refreshClock() }
}

data class Dependencies(val repository: CountdownRepository, val clock: SystemTimeSource, val countdown: CalculateCountdown, val info: AppInfoProvider, val defaultName: String, val systemMotion: () -> Boolean)
