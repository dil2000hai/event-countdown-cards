package com.eventcountdown.cards.presentation.game

import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.config.GameConfig
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.core.ui.EventCardModel
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.time.TimeSource
import com.eventcountdown.cards.domain.usecase.CalculateCountdown
import com.eventcountdown.cards.domain.usecase.ValidateEvent
import com.eventcountdown.cards.presentation.app.CollectionViewModel
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SavedEvent(val id: String, val isEdit: Boolean, val haptic: Boolean)

class GameViewModel(
    repository: CountdownRepository, private val clock: TimeSource, private val countdown: CalculateCountdown,
    private val route: Screen.Game, private val defaultName: String
) : CollectionViewModel(repository) {
    private val mutableState = MutableStateFlow(GameUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<SavedEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var debounce: Job? = null
    private var generation = repository.newDraftGeneration()
    init {
        viewModelScope.launch {
            repository.load()
            val source = route.eventId?.let { id -> repository.state.value.events.find { it.id == id } }
            val restored = if (route.eventId == null && !route.fresh) repository.state.value.draft else null
            val draft = when {
                route.eventId != null && source == null -> null
                source != null -> EventDraft(if (route.replan) UUID.randomUUID().toString() else source.id,
                    if (route.replan) DraftMode.Replan else DraftMode.Edit, if (route.replan) null else source.id,
                    source.name, source.category, if (route.replan) clock.today().plusDays(30) else source.eventDate,
                    source.picture, source.notes, if (route.replan) false else source.pinnedInApp, clock.now().toEpochMilli())
                restored != null -> restored
                else -> freshDraft()
            }
            mutableState.update { it.copy(loading = false, draft = draft, restored = restored != null, today = clock.today(), error = if (draft == null) R.string.missing_card else null) }
            render()
            if (draft != null) persistSoon()
        }
        viewModelScope.launch { repository.state.collect { render() } }
    }
    private fun freshDraft() = EventDraft(UUID.randomUUID().toString(), DraftMode.Create, null, defaultName, Category.Birthday, clock.today().plusDays(30), Picture.Birthday)
    private fun render() {
        val d = mutableState.value.draft
        val event = d?.let { Event(it.draftId, it.name, it.category, it.eventDate, it.picture, it.notes, it.pinnedInApp, 0, 0) }
        mutableState.update { it.copy(preview = event?.let { e -> EventCardModel(e, countdown(e)) }, style = repository.state.value.settings.cardStyle, today = clock.today()) }
    }
    private fun limited(text: String, max: Int): String = if (text.codePointCount(0, text.length) <= max) text else text.substring(0, text.offsetByCodePoints(0, max))
    private fun edit(transform: (EventDraft) -> EventDraft) {
        if (mutableState.value.isSaving) return
        mutableState.update { it.copy(draft = it.draft?.let(transform)?.copy(updatedAt = clock.now().toEpochMilli()), error = null, nameError = false, notesError = false, dateError = false) }
        render(); persistSoon()
    }
    fun name(value: String) = edit { it.copy(name = limited(value, 60)) }
    fun notes(value: String) = edit { it.copy(notes = limited(value, 280)) }
    fun category(value: Category) = edit { it.copy(category = value) }
    fun picture(value: Picture) = edit { it.copy(picture = value) }
    fun pin(value: Boolean) = edit { it.copy(pinnedInApp = value) }
    fun date(value: LocalDate) { edit { it.copy(eventDate = value) }; mutableState.update { it.copy(selectedDateMillis = value.toEpochDay() * 86400000L, displayedMonthMillis = null) }; datePicker(false) }
    fun relativeDate(days: Long) = date(clock.today().plusDays(days))
    fun datePicker(visible: Boolean) { mutableState.update { it.copy(datePickerVisible = visible) } }
    fun pickerSelection(date: Long?, month: Long) { mutableState.update { it.copy(selectedDateMillis = date, displayedMonthMillis = month) } }
    fun help(visible: Boolean) { mutableState.update { it.copy(helpVisible = visible) } }
    fun clearDialog(visible: Boolean) { mutableState.update { it.copy(clearVisible = visible) } }
    fun clear() {
        debounce?.cancel()
        viewModelScope.launch {
            if (repository.clearDraft()) {
                generation = repository.newDraftGeneration()
                mutableState.update { it.copy(draft = freshDraft(), restored = false, clearVisible = false, error = null) }
                render(); persistSoon()
            } else mutableState.update { it.copy(error = R.string.write_error) }
        }
    }
    private fun persistSoon() {
        debounce?.cancel()
        val draft = mutableState.value.draft ?: return
        generation = repository.newDraftGeneration()
        val token = generation
        debounce = viewModelScope.launch {
            delay(GameConfig.DRAFT_DEBOUNCE_MS)
            if (!repository.saveDraft(draft, token)) mutableState.update { it.copy(error = R.string.draft_error) }
        }
    }
    fun save() {
        val draft = mutableState.value.draft ?: return
        if (mutableState.value.isSaving) return
        val validation = ValidateEvent()(draft, clock.today())
        if (!validation.valid) {
            mutableState.update { it.copy(nameError = validation.nameInvalid, dateError = validation.dateInvalid, notesError = validation.notesInvalid, error = R.string.validation_error) }
            return
        }
        mutableState.update { it.copy(isSaving = true, error = null) }
        debounce?.cancel()
        viewModelScope.launch {
            debounce?.join()
            val id = repository.saveEvent(draft)
            if (id != null) {
                generation = repository.newDraftGeneration()
                mutableState.update { it.copy(isSaving = false, draft = draft.copy(draftId = id, sourceEventId = id, mode = DraftMode.Edit), restored = false) }
                pause()
                channel.send(SavedEvent(id, draft.mode == DraftMode.Edit, repository.state.value.settings.hapticsEnabled))
            } else mutableState.update { it.copy(isSaving = false, error = if (repository.state.value.problem == StorageProblem.Missing) R.string.missing_card else R.string.write_error) }
        }
    }
    override fun pause() { super.pause() }
}
