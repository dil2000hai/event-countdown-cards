package com.eventcountdown.cards.presentation.settings

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*

data class SettingsUiState(val loading: Boolean = true, val settings: AppSettings = AppSettings(), val preview: EventCardModel? = null,
    val busy: Boolean = false, val resetVisible: Boolean = false, val error: Int? = null)
