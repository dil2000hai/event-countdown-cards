package com.eventcountdown.cards.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.core.config.GameConfig
import com.eventcountdown.cards.domain.repository.CountdownRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class CollectionViewModel(protected val repository: CountdownRepository) : ViewModel() {
    private var refreshJob: Job? = null
    fun resume() {
        repository.refreshClock()
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            while (isActive) { delay(GameConfig.REFRESH_INTERVAL_MS); repository.refreshClock() }
        }
    }
    open fun pause() { refreshJob?.cancel(); refreshJob = null }
    fun retry() { viewModelScope.launch { repository.load(true) } }
}
