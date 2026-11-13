package com.eventcountdown.cards.presentation.detail

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*

data class EventDetailUiState(
    val loading: Boolean = true, val card: EventCardModel? = null, val style: CardStyle = CardStyle.Cream,
    val widgetVisible: Boolean = false, val archiveVisible: Boolean = false, val deleteVisible: Boolean = false,
    val busy: Boolean = false, val error: Int? = null
)
