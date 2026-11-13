package com.eventcountdown.cards.presentation.menu

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class MenuViewModel(repository: CountdownRepository, private val countdown: CalculateCountdown) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(MenuUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<Int>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { render() } }
    }
    fun filter(category: Category?) { mutableState.update { it.copy(filter = category) }; render() }
    private fun render() {
        val data = repository.state.value
        val upcoming = data.events.map { EventCardModel(it, countdown(it)) }.filter { it.countdown.calendarDays >= 0 && !it.countdown.isArchived }
            .sortedWith(compareBy({ it.event.eventDate }, { it.event.name }, { it.event.id }))
        val filtered = upcoming.filter { mutableState.value.filter == null || it.event.category == mutableState.value.filter }
        mutableState.update { it.copy(loading = data.loading, cards = filtered.filterNot { c -> c.event.pinnedInApp }, pinnedCards = filtered.filter { c -> c.event.pinnedInApp },
            totalUpcoming = upcoming.size, nearestName = upcoming.firstOrNull()?.event?.name, restoredDraft = data.draft != null,
            style = data.settings.cardStyle, problem = data.problem, imageFallback = data.imageFallback) }
    }
    fun pin(id: String) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val success = repository.togglePin(id)
            val pinned = repository.state.value.events.find { it.id == id }?.pinnedInApp == true
            mutableState.update { it.copy(busy = false) }
            channel.send(if (success) { if (pinned) R.string.pinned_notice else R.string.unpinned_notice } else R.string.write_error)
        }
    }
}
