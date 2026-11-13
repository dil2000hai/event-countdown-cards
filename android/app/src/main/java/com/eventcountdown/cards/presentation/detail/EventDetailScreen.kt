package com.eventcountdown.cards.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*

@Composable
fun EventDetailRoute(route: Screen.EventDetail, onBack: () -> Unit, onMenu: () -> Unit, onEdit: () -> Unit, onReplan: () -> Unit, onChanged: () -> Unit) {
    val model: EventDetailViewModel = viewModel(key = "detail-${route.eventId}", factory = AppViewModelFactory(route))
    val state by model.state.collectAsStateWithLifecycle()
    RouteLifecycle(model::resume, model::pause)
    LaunchedEffect(Unit) { model.events.collect { onChanged() } }
    EventDetailScreen(state, onBack, onMenu, onEdit, onReplan, model::pin, model::widget, model::archiveDialog, model::deleteDialog, model::archive, model::delete, model::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(state: EventDetailUiState, onBack: () -> Unit, onMenu: () -> Unit, onEdit: () -> Unit, onReplan: () -> Unit,
    onPin: () -> Unit, onWidget: (Boolean) -> Unit, onArchiveDialog: (Boolean) -> Unit, onDeleteDialog: (Boolean) -> Unit,
    onArchive: (Boolean) -> Unit, onDelete: () -> Unit, onRetry: () -> Unit) {
    ScreenBackdrop {
        PageColumn(Modifier.widthIn(max = 640.dp).align(Alignment.TopCenter)) {
            FeatureHeader(R.string.detail_title, onBack)
            if (state.loading) CircularProgressIndicator(color = CelebrationGreen)
            state.error?.let { InlineStatus(it, true, onRetry) }
            val card = state.card
            if (card != null) {
                EventPictureCard(card, state.style)
                RaisedPanel {
                    when {
                        card.countdown.calendarDays > 0 -> {
                            Text(stringResource(R.string.until_begins), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.duration, card.countdown.wholeHoursUntilStart, card.countdown.remainingMinutes), style = MaterialTheme.typography.bodyLarge)
                        }
                        card.countdown.calendarDays == 0L -> Text(stringResource(R.string.enjoy_day), style = MaterialTheme.typography.titleLarge)
                        else -> { Text(dateLabel(card.event.eventDate), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.past_kept)) }
                    }
                }
                RaisedPanel {
                    Text(stringResource(R.string.notes), style = MaterialTheme.typography.titleLarge)
                    Text(card.event.notes.ifEmpty { stringResource(R.string.no_notes) }, style = MaterialTheme.typography.bodyLarge)
                }
                if (card.countdown.calendarDays >= 0 && !card.countdown.isArchived) {
                    PrimaryButton(R.string.edit, onEdit, enabled = !state.busy)
                    SecondaryButton(if (card.event.pinnedInApp) R.string.unpin else R.string.pin, onPin, Modifier.fillMaxWidth(), !state.busy)
                    SecondaryButton(R.string.widget, { onWidget(true) }, Modifier.fillMaxWidth(), !state.busy)
                    SecondaryButton(R.string.archive, { onArchiveDialog(true) }, Modifier.fillMaxWidth(), !state.busy)
                } else {
                    PrimaryButton(R.string.replan, onReplan, enabled = !state.busy)
                    if (card.countdown.isArchived && card.countdown.calendarDays >= 0) SecondaryButton(R.string.restore, { onArchive(false) }, Modifier.fillMaxWidth(), !state.busy)
                }
                SecondaryButton(R.string.delete, { onDeleteDialog(true) }, Modifier.fillMaxWidth(), !state.busy)
            } else if (!state.loading) InlineStatus(R.string.missing_card)
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        }
    }
    if (state.archiveVisible) ConfirmationDialog(R.string.archive_title, stringResource(R.string.archive_body), R.string.archive, state.busy, { onArchive(true) }, { onArchiveDialog(false) })
    if (state.deleteVisible) ConfirmationDialog(R.string.delete_title, stringResource(R.string.delete_body, state.card?.event?.name.orEmpty()), R.string.delete, state.busy, onDelete, { onDeleteDialog(false) })
    if (state.widgetVisible) ModalBottomSheet(onDismissRequest = { onWidget(false) }, containerColor = Paper, contentColor = ForestBackground) {
        Column(Modifier.fillMaxWidth().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.widget_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.widget_body))
            state.error?.let { Text(stringResource(it)) }
            PrimaryButton(if (state.card?.event?.pinnedInApp == true) R.string.unpin else R.string.pin, onPin, enabled = !state.busy)
            SecondaryButton(R.string.close, { onWidget(false) }, Modifier.fillMaxWidth(), light = true)
        }
    }
}
