package com.eventcountdown.cards.presentation.gameover

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*

data class GameOverUiState(val loading: Boolean = true, val card: EventCardModel? = null, val style: CardStyle = CardStyle.Cream, val isEdit: Boolean = false, val problem: StorageProblem? = null)
