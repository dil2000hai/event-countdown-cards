package com.eventcountdown.cards.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*
import com.eventcountdown.cards.domain.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

sealed interface GameAction {
    data class Name(val value: String) : GameAction
    data class Notes(val value: String) : GameAction
    data class ChooseCategory(val value: Category) : GameAction
    data class ChoosePicture(val value: Picture) : GameAction
    data class Pin(val value: Boolean) : GameAction
    data class RelativeDate(val days: Long) : GameAction
    data class Date(val value: LocalDate) : GameAction
    data class Picker(val visible: Boolean) : GameAction
    data class PickerState(val date: Long?, val month: Long) : GameAction
    data class Help(val visible: Boolean) : GameAction
    data class ClearDialog(val visible: Boolean) : GameAction
    data object Clear : GameAction
    data object Save : GameAction
}

@Composable
fun GameRoute(route: Screen.Game, onBack: () -> Unit, onSaved: (SavedEvent) -> Unit) {
    val model: GameViewModel = viewModel(key = route.sessionId, factory = AppViewModelFactory(route))
    val state by model.state.collectAsStateWithLifecycle()
    RouteLifecycle(model::resume, model::pause)
    DisposableEffect(Unit) { onDispose { model.pause() } }
    LaunchedEffect(Unit) { model.events.collect { onSaved(it) } }
    GameScreen(state, onBack) { action ->
        when (action) {
            is GameAction.Name -> model.name(action.value)
            is GameAction.Notes -> model.notes(action.value)
            is GameAction.ChooseCategory -> model.category(action.value)
            is GameAction.ChoosePicture -> model.picture(action.value)
            is GameAction.Pin -> model.pin(action.value)
            is GameAction.RelativeDate -> model.relativeDate(action.days)
            is GameAction.Date -> model.date(action.value)
            is GameAction.Picker -> model.datePicker(action.visible)
            is GameAction.PickerState -> model.pickerSelection(action.date, action.month)
            is GameAction.Help -> model.help(action.visible)
            is GameAction.ClearDialog -> model.clearDialog(action.visible)
            GameAction.Clear -> model.clear()
            GameAction.Save -> model.save()
        }
    }
}

@Composable
fun GameScreen(state: GameUiState, onBack: () -> Unit, onAction: (GameAction) -> Unit) {
    AppTheme(light = true) {
        ScreenBackdrop(light = true) {
            BoxWithConstraints(Modifier.widthIn(max = 600.dp).fillMaxSize().align(Alignment.TopCenter).safeDrawingPadding().imePadding().padding(horizontal = 18.dp)) {
                val compact = maxHeight < 420.dp
                Column(Modifier.fillMaxSize()) {
                    if (!compact) GameHeader(state, onBack, onAction)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (compact) GameHeader(state, onBack, onAction)
                        if (state.loading) CircularProgressIndicator()
                        state.preview?.let { EventPictureCard(it, state.style) }
                        val draft = state.draft
                        if (draft != null) {
                            if (state.restored) InlineStatus(R.string.restored_draft)
                            RaisedPanel {
                                val focus = remember { FocusRequester() }
                                LaunchedEffect(state.nameError) { if (state.nameError) focus.requestFocus() }
                                OutlinedTextField(draft.name, { onAction(GameAction.Name(it)) }, Modifier.fillMaxWidth().focusRequester(focus), enabled = !state.isSaving,
                                    label = { Text(stringResource(R.string.event_name)) }, isError = state.nameError, singleLine = true,
                                    supportingText = { Text(stringResource(if (state.nameError) R.string.name_error else R.string.name_count, draft.name.codePointCount(0, draft.name.length))) })
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Category.entries.forEach { category -> ChoiceChip(categoryLabel(category), draft.category == category, { onAction(GameAction.ChooseCategory(category)) }, true, !state.isSaving) }
                                }
                                SecondaryButton(R.string.choose_date, { onAction(GameAction.Picker(true)) }, Modifier.fillMaxWidth(), !state.isSaving, true)
                                Text(dateLabel(draft.eventDate), style = MaterialTheme.typography.titleLarge)
                                if (state.dateError) Text(stringResource(R.string.date_error))
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SecondaryButton(R.string.today, { onAction(GameAction.RelativeDate(0)) }, enabled = !state.isSaving, light = true)
                                    SecondaryButton(R.string.plus_seven, { onAction(GameAction.RelativeDate(7)) }, enabled = !state.isSaving, light = true)
                                    SecondaryButton(R.string.plus_thirty, { onAction(GameAction.RelativeDate(30)) }, enabled = !state.isSaving, light = true)
                                }
                                Text(stringResource(R.string.choose_picture), style = MaterialTheme.typography.titleLarge)
                                Picture.entries.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        pair.forEach { picture ->
                                            val selected = picture == draft.picture
                                            val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
                                            Surface(onClick = { onAction(GameAction.ChoosePicture(picture)) }, enabled = !state.isSaving,
                                                modifier = Modifier.weight(1f).semantics { stateDescription = description },
                                                shape = RoundedCornerShape(16.dp), color = PaperRaised,
                                                border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) CelebrationPink else PaperBorder)) {
                                                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Image(painterResource(pictureResource(picture)), stringResource(pictureLabel(picture)), Modifier.size(72.dp), contentScale = ContentScale.Fit)
                                                    Text(if (selected) stringResource(R.string.selected_mark, stringResource(pictureLabel(picture))) else stringResource(pictureLabel(picture)), style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                                OutlinedTextField(draft.notes, { onAction(GameAction.Notes(it)) }, Modifier.fillMaxWidth().heightIn(min = 112.dp), enabled = !state.isSaving,
                                    label = { Text(stringResource(R.string.notes)) }, isError = state.notesError,
                                    supportingText = { Text(stringResource(if (state.notesError) R.string.notes_error else R.string.notes_count, draft.notes.codePointCount(0, draft.notes.length))) })
                                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(stringResource(R.string.pin_title)); Text(stringResource(R.string.pin_hint), style = MaterialTheme.typography.bodyMedium) }
                                    val pinDescription = stringResource(if (draft.pinnedInApp) R.string.on_state else R.string.off_state)
                                    Switch(draft.pinnedInApp, { onAction(GameAction.Pin(it)) }, Modifier.semantics { stateDescription = pinDescription }, enabled = !state.isSaving)
                                }
                                SecondaryButton(R.string.clear_draft, { onAction(GameAction.ClearDialog(true)) }, enabled = !state.isSaving, light = true)
                            }
                        }
                        state.error?.let { InlineStatus(it, true) }
                        Spacer(Modifier.height(8.dp))
                        if (compact) GameFooter(state, onAction)
                    }
                    if (!compact) GameFooter(state, onAction)
                }
            }
        }
        if (state.helpVisible) AlertDialog(onDismissRequest = { onAction(GameAction.Help(false)) }, title = { Text(stringResource(R.string.help_title)) }, text = { Text(stringResource(R.string.help_body)) }, confirmButton = { TextButton({ onAction(GameAction.Help(false)) }) { Text(stringResource(R.string.close)) } })
        if (state.clearVisible) ConfirmationDialog(R.string.clear_title, stringResource(R.string.clear_body), R.string.clear_draft, state.isSaving, { onAction(GameAction.Clear) }, { onAction(GameAction.ClearDialog(false)) })
        if (state.datePickerVisible && state.draft != null) EventDatePicker(state, onAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePicker(state: GameUiState, onAction: (GameAction) -> Unit) {
    val start = state.today.toEpochDay() * 86400000L
    val end = state.today.plusYears(10).toEpochDay() * 86400000L
    val bounds = remember(start, end) { object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in start..end
        override fun isSelectableYear(year: Int): Boolean = year in state.today.year..state.today.plusYears(10).year
    } }
    val picker = rememberDatePickerState(initialSelectedDateMillis = state.selectedDateMillis ?: state.draft?.eventDate?.toEpochDay()?.times(86400000L),
        initialDisplayedMonthMillis = state.displayedMonthMillis, yearRange = state.today.year..state.today.plusYears(10).year, selectableDates = bounds)
    LaunchedEffect(picker) { snapshotFlow { picker.selectedDateMillis to picker.displayedMonthMillis }.collect { onAction(GameAction.PickerState(it.first, it.second)) } }
    DatePickerDialog(onDismissRequest = { onAction(GameAction.Picker(false)) },
        confirmButton = { TextButton({ picker.selectedDateMillis?.let { onAction(GameAction.Date(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())) } }, enabled = picker.selectedDateMillis?.let { it in start..end } == true) { Text(stringResource(R.string.set_date)) } },
        dismissButton = { TextButton({ onAction(GameAction.Picker(false)) }) { Text(stringResource(R.string.cancel)) } }) {
        DatePicker(picker, title = { Text(stringResource(R.string.date_title), Modifier.padding(20.dp)) })
    }
}

@Composable
private fun GameHeader(state: GameUiState, onBack: () -> Unit, onAction: (GameAction) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { FeatureHeader(if (state.draft?.mode == DraftMode.Edit) R.string.edit_event else R.string.new_event, onBack, true) }
        TextButton({ onAction(GameAction.Help(true)) }) { Text(stringResource(R.string.help)) }
    }
}

@Composable
private fun GameFooter(state: GameUiState, onAction: (GameAction) -> Unit) {
    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.privacy_hint), style = MaterialTheme.typography.bodyMedium, color = ForestBackground)
        PrimaryButton(if (state.isSaving) R.string.saving else R.string.save, { onAction(GameAction.Save) }, enabled = !state.loading && !state.isSaving && state.draft != null, loading = state.isSaving)
    }
}
