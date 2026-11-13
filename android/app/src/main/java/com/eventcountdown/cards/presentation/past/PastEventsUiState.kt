package com.eventcountdown.cards.presentation.past

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*

data class PastEventsUiState(val loading: Boolean = true, val cards: List<EventCardModel> = emptyList(), val filter: PastFilter = PastFilter.All,
    val style: CardStyle = CardStyle.Cream, val pendingDelete: EventCardModel? = null, val busy: Boolean = false, val error: Int? = null)
