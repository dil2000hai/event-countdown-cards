package com.eventcountdown.cards.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.core.config.GameConfig
import com.eventcountdown.cards.domain.repository.CountdownRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class SplashViewModel(repository: CountdownRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SplashUiState())
    val state = mutableState.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch { repository.load(); mutableState.update { it.copy(animationsEnabled = repository.state.value.settings.gentleAnimationsEnabled) } }
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            mutableState.update { it.copy(isFinished = true) }
            _finished.value = true
        }
    }
}
