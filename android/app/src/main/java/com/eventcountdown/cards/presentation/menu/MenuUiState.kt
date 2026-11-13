package com.eventcountdown.cards.presentation.menu

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*

data class MenuUiState(

    val loading: Boolean = true,

    val cards: List<EventCardModel> = emptyList(),

    val pinnedCards: List<EventCardModel> = emptyList(),

    val filter: Category? = null,

    val totalUpcoming: Int = 0,

    val nearestName: String? = null,

    val restoredDraft: Boolean = false,

    val style: CardStyle = CardStyle.Cream,

    val problem: StorageProblem? = null,

    val imageFallback: Boolean = false,

    val busy: Boolean = false

)
