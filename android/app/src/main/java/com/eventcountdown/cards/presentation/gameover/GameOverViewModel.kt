package com.eventcountdown.cards.presentation.gameover

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.CardStyle
import com.eventcountdown.cards.domain.model.StorageProblem
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class GameOverViewModel(repository: CountdownRepository, countdown: CalculateCountdown, id: String, edit: Boolean) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(GameOverUiState(isEdit = edit))
    val state = mutableState.asStateFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { data ->
            mutableState.update { it.copy(loading = data.loading, card = data.events.find { e -> e.id == id }?.let { e -> EventCardModel(e, countdown(e)) }, style = data.settings.cardStyle, problem = data.problem) }
        } }
    }
}
