package com.eventcountdown.cards.presentation.game

import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*
import java.time.LocalDate

data class GameUiState(
    val loading: Boolean = true, val draft: EventDraft? = null, val preview: EventCardModel? = null,
    val style: CardStyle = CardStyle.Cream, val isSaving: Boolean = false, val restored: Boolean = false,
    val nameError: Boolean = false, val dateError: Boolean = false, val notesError: Boolean = false,
    val error: Int? = null, val datePickerVisible: Boolean = false, val helpVisible: Boolean = false,
    val clearVisible: Boolean = false, val selectedDateMillis: Long? = null, val displayedMonthMillis: Long? = null,
    val today: LocalDate = LocalDate.of(2000, 1, 1)
)
