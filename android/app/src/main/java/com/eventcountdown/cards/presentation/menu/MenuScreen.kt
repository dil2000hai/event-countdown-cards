package com.eventcountdown.cards.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*
import com.eventcountdown.cards.domain.model.Category
import com.eventcountdown.cards.domain.model.StorageProblem

@Composable
fun MenuRoute(onCreate: () -> Unit, onDetail: (String) -> Unit, onPast: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit) {
    val model: MenuViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val notices = mapOf(R.string.pinned_notice to stringResource(R.string.pinned_notice), R.string.unpinned_notice to stringResource(R.string.unpinned_notice), R.string.write_error to stringResource(R.string.write_error))
    LaunchedEffect(Unit) { model.events.collect { snackbar.showSnackbar(notices.getValue(it)) } }
    RouteLifecycle(model::resume, model::pause)
    MenuScreen(state, onCreate, onDetail, model::filter, model::pin, model::retry, onPast, onSettings, onAbout, snackbar)
}

@Composable
fun MenuScreen(state: MenuUiState, onCreate: () -> Unit, onDetail: (String) -> Unit, onFilter: (Category?) -> Unit, onPin: (String) -> Unit,
    onRetry: () -> Unit, onPast: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit, snackbar: SnackbarHostState) {
    ScreenBackdrop {
        BoxWithConstraints(Modifier.widthIn(max = 840.dp).fillMaxSize().align(Alignment.TopCenter).safeDrawingPadding().padding(horizontal = 18.dp)) {
            val compact = maxHeight < 520.dp
            Column(Modifier.fillMaxSize()) {
                if (!compact) MenuHeader(onSettings)
                LazyVerticalGrid(columns = GridCells.Adaptive(280.dp), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    if (compact) item(span = { GridItemSpan(maxLineSpan) }) { MenuHeader(onSettings) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RaisedPanel {
                            Text(stringResource(if (state.totalUpcoming > 0) R.string.hero_title else R.string.hero_empty), style = MaterialTheme.typography.titleLarge)
                            if (state.totalUpcoming > 0) Text(pluralStringResource(R.plurals.upcoming_count, state.totalUpcoming, state.totalUpcoming), style = MaterialTheme.typography.bodyMedium)
                            state.nearestName?.let { Text(stringResource(R.string.nearest, it), style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceChip(R.string.all, state.filter == null, { onFilter(null) })
                            ChoiceChip(R.string.birthdays, state.filter == Category.Birthday, { onFilter(Category.Birthday) })
                            ChoiceChip(R.string.holidays, state.filter == Category.Holiday, { onFilter(Category.Holiday) })
                            ChoiceChip(R.string.trips, state.filter == Category.Trip, { onFilter(Category.Trip) })
                        }
                    }
                    if (state.loading) item(span = { GridItemSpan(maxLineSpan) }) { CircularProgressIndicator(color = CelebrationGreen) }
                    if (state.problem != null) item(span = { GridItemSpan(maxLineSpan) }) { InlineStatus(when (state.problem) { StorageProblem.NewerSchema -> R.string.newer_schema; StorageProblem.Write -> R.string.write_error; else -> R.string.read_error }, true, onRetry) }
                    if (state.imageFallback) item(span = { GridItemSpan(maxLineSpan) }) { InlineStatus(R.string.fallback_notice) }
                    if (!state.loading && state.cards.isEmpty() && state.pinnedCards.isEmpty() && state.problem == null) item(span = { GridItemSpan(maxLineSpan) }) {
                        RaisedPanel {
                            Image(painterResource(R.drawable.logo), null, Modifier.size(88.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
                            Text(stringResource(if (state.filter == null) R.string.empty_title else R.string.filter_empty), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.empty_body), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (state.pinnedCards.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Text(stringResource(R.string.pinned_section), style = MaterialTheme.typography.titleLarge, color = LightText) }
                    items(state.pinnedCards, key = { it.event.id }) { card -> EventPictureCard(card, state.style, onClick = { onDetail(card.event.id) }, onPin = { onPin(card.event.id) }, pinEnabled = !state.busy) }
                    if (state.cards.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Text(stringResource(R.string.coming_up), style = MaterialTheme.typography.titleLarge, color = LightText) }
                    items(state.cards, key = { it.event.id }) { card -> EventPictureCard(card, state.style, onClick = { onDetail(card.event.id) }, onPin = { onPin(card.event.id) }, pinEnabled = !state.busy) }
                    if (compact) item(span = { GridItemSpan(maxLineSpan) }) { MenuFooter(state, onCreate, onPast, onAbout, snackbar) }
                }
                if (!compact) MenuFooter(state, onCreate, onPast, onAbout, snackbar)
            }
        }
    }
}

@Composable
private fun MenuHeader(onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.privacy_short), style = MaterialTheme.typography.labelSmall, color = PaperBorder)
            Text(stringResource(R.string.countdowns), style = MaterialTheme.typography.headlineMedium, color = LightText)
        }
        SecondaryButton(R.string.settings, onSettings)
    }
}

@Composable
private fun MenuFooter(state: MenuUiState, onCreate: () -> Unit, onPast: () -> Unit, onAbout: () -> Unit, snackbar: SnackbarHostState) {
    Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(if (state.restoredDraft) R.string.unfinished else R.string.choose_day), style = MaterialTheme.typography.bodyMedium, color = LightText)
        SnackbarHost(snackbar)
        PrimaryButton(R.string.create, onCreate, enabled = !state.loading && state.problem != StorageProblem.Read && state.problem != StorageProblem.NewerSchema)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onPast) { Text(stringResource(R.string.past_events), color = LightText) }
            TextButton(onAbout) { Text(stringResource(R.string.about), color = LightText) }
        }
    }
}
