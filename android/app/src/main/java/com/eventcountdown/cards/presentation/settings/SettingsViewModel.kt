package com.eventcountdown.cards.presentation.settings

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class SettingsViewModel(repository: CountdownRepository, countdown: CalculateCountdown) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state = mutableState.asStateFlow()
    private var retrySettings: AppSettings? = null
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { data ->
            val preview = data.events.map { EventCardModel(it, countdown(it)) }.filter { it.countdown.calendarDays >= 0 && !it.countdown.isArchived }.minByOrNull { it.event.eventDate }
            mutableState.update { it.copy(loading = data.loading, settings = if (it.busy) it.settings else data.settings, preview = preview,
                error = if (data.problem != null) R.string.setting_error else it.error) }
        } }
    }
    fun style(value: CardStyle) = save(mutableState.value.settings.copy(cardStyle = value))
    fun animations(value: Boolean) = save(mutableState.value.settings.copy(gentleAnimationsEnabled = value))
    fun haptics(value: Boolean) = save(mutableState.value.settings.copy(hapticsEnabled = value))
    fun resetDialog(visible: Boolean) { mutableState.update { it.copy(resetVisible = visible) } }
    fun reset() = save(AppSettings())
    fun retrySave() { val value = retrySettings; if (value == null) retry() else save(value) }
    private fun save(value: AppSettings) {
        if (mutableState.value.busy) return
        val previous = repository.state.value.settings
        mutableState.update { it.copy(settings = value, busy = true, error = null) }
        viewModelScope.launch {
            val success = repository.saveSettings(value)
            retrySettings = if (success) null else value
            mutableState.update { it.copy(busy = false, settings = if (success) value else previous, resetVisible = if (success) false else it.resetVisible, error = if (success) null else R.string.setting_error) }
        }
    }
}
