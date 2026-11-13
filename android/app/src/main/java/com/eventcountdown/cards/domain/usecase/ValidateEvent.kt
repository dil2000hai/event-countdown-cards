package com.eventcountdown.cards.domain.usecase

import com.eventcountdown.cards.domain.model.EventDraft
import java.time.LocalDate

data class Validation(val nameInvalid: Boolean, val dateInvalid: Boolean, val notesInvalid: Boolean) {
    val valid: Boolean get() = !nameInvalid && !dateInvalid && !notesInvalid
}

class ValidateEvent {
    operator fun invoke(draft: EventDraft, today: LocalDate): Validation = Validation(
        draft.name.isBlank() || draft.name.codePointCount(0, draft.name.length) > 60,
        draft.eventDate < today || draft.eventDate > today.plusYears(10),
        draft.notes.codePointCount(0, draft.notes.length) > 280
    )
}
