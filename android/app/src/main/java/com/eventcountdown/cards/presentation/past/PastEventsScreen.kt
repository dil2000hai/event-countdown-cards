package com.eventcountdown.cards.presentation.past

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
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
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*

@Composable
fun PastEventsRoute(onBack: () -> Unit, onMenu: () -> Unit, onDetail: (String) -> Unit) {
    val model: PastEventsViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    RouteLifecycle(model::resume, model::pause)
    PastEventsScreen(state, onBack, onMenu, onDetail, model::filter, model::deleteDialog, model::delete, model::retry)
}

@Composable
fun PastEventsScreen(state: PastEventsUiState, onBack: () -> Unit, onMenu: () -> Unit, onDetail: (String) -> Unit, onFilter: (PastFilter) -> Unit,
    onDeleteDialog: (EventCardModel?) -> Unit, onDelete: () -> Unit, onRetry: () -> Unit) {
    AppTheme(light = true) {
        ScreenBackdrop(light = true) {
            BoxWithConstraints(Modifier.widthIn(max = 840.dp).fillMaxSize().align(Alignment.TopCenter).safeDrawingPadding().padding(18.dp)) {
                val compact = maxHeight < 520.dp
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!compact) PastHeader(state, onBack, onFilter)
                    LazyVerticalGrid(columns = GridCells.Adaptive(280.dp), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (compact) item(span = { GridItemSpan(maxLineSpan) }) { PastHeader(state, onBack, onFilter) }
                        if (state.loading) item(span = { GridItemSpan(maxLineSpan) }) { CircularProgressIndicator() }
                        state.error?.let { message -> item(span = { GridItemSpan(maxLineSpan) }) { InlineStatus(message, true, onRetry) } }
                        if (!state.loading && state.cards.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                            RaisedPanel { Text(stringResource(R.string.past_empty_title), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.past_empty_body)) }
                        }
                        items(state.cards, key = { it.event.id }) { card ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EventPictureCard(card, state.style, onClick = { onDetail(card.event.id) })
                                SecondaryButton(R.string.delete, { onDeleteDialog(card) }, enabled = !state.busy, light = true)
                            }
                        }
                        if (compact) item(span = { GridItemSpan(maxLineSpan) }) { SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), light = true) }
                    }
                    if (!compact) SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), light = true)
                }
            }
        }
        state.pendingDelete?.let { card -> ConfirmationDialog(R.string.delete_title, stringResource(R.string.delete_body, card.event.name), R.string.delete, state.busy, onDelete, { onDeleteDialog(null) }) }
    }
}

@Composable
private fun PastHeader(state: PastEventsUiState, onBack: () -> Unit, onFilter: (PastFilter) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureHeader(R.string.past_title, onBack, true)
        Text(stringResource(R.string.past_subtitle), style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(R.string.all, state.filter == PastFilter.All, { onFilter(PastFilter.All) }, true)
            ChoiceChip(R.string.happened, state.filter == PastFilter.Happened, { onFilter(PastFilter.Happened) }, true)
            ChoiceChip(R.string.archived, state.filter == PastFilter.Archived, { onFilter(PastFilter.Archived) }, true)
        }
    }
}
