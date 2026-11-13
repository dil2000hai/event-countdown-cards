package com.eventcountdown.cards.presentation.detail

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.CardStyle
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class EventDetailViewModel(repository: CountdownRepository, countdown: CalculateCountdown, private val id: String) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(EventDetailUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<Unit>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { data ->
            mutableState.update { it.copy(loading = data.loading, card = data.events.find { e -> e.id == id }?.let { e -> EventCardModel(e, countdown(e)) }, style = data.settings.cardStyle,
                error = if (data.problem != null) R.string.read_error else it.error) }
        } }
    }
    fun widget(visible: Boolean) { mutableState.update { it.copy(widgetVisible = visible) } }
    fun archiveDialog(visible: Boolean) { mutableState.update { it.copy(archiveVisible = visible) } }
    fun deleteDialog(visible: Boolean) { mutableState.update { it.copy(deleteVisible = visible) } }
    fun pin() = mutate(false) { repository.togglePin(id) }
    fun archive(archived: Boolean) = mutate(true) { repository.archive(id, archived) }
    fun delete() = mutate(true) { repository.delete(id) }
    private fun mutate(leave: Boolean, action: suspend () -> Boolean) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val success = action()
            mutableState.update { it.copy(busy = false, error = if (success) null else R.string.write_error) }
            if (success && leave) {
                mutableState.update { it.copy(archiveVisible = false, deleteVisible = false) }
                channel.send(Unit)
            }
        }
    }
}
