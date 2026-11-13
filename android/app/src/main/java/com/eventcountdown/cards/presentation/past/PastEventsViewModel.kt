package com.eventcountdown.cards.presentation.past

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.CardStyle
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.time.TimeSource
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PastFilter { All, Happened, Archived }


class PastEventsViewModel(repository: CountdownRepository, private val countdown: CalculateCountdown, private val clock: TimeSource) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(PastEventsUiState())
    val state = mutableState.asStateFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { render() } }
    }
    fun filter(value: PastFilter) { mutableState.update { it.copy(filter = value) }; render() }
    private fun render() {
        val data = repository.state.value
        val cards = data.events.map { EventCardModel(it, countdown(it)) }.filter {
            when (mutableState.value.filter) {
                PastFilter.All -> it.countdown.calendarDays < 0 || it.countdown.isArchived
                PastFilter.Happened -> it.countdown.calendarDays < 0
                PastFilter.Archived -> it.countdown.isArchived
            }
        }.sortedWith(compareByDescending<EventCardModel> { it.event.manuallyArchivedAt?.let { epoch -> java.time.Instant.ofEpochMilli(epoch).atZone(clock.zone()).toLocalDate() } ?: it.event.eventDate }
            .thenByDescending { it.event.eventDate }.thenBy { it.event.name }.thenBy { it.event.id })
        mutableState.update { it.copy(loading = data.loading, cards = cards, style = data.settings.cardStyle, error = if (data.problem != null) R.string.read_error else null) }
    }
    fun deleteDialog(card: EventCardModel?) { mutableState.update { it.copy(pendingDelete = card) } }
    fun delete() {
        val id = mutableState.value.pendingDelete?.event?.id ?: return
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val success = repository.delete(id)
            mutableState.update { it.copy(busy = false, pendingDelete = if (success) null else it.pendingDelete, error = if (success) null else R.string.write_error) }
        }
    }
}
