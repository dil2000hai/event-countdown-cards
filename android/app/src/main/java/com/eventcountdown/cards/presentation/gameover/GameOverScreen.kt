package com.eventcountdown.cards.presentation.gameover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*

@Composable
fun GameOverRoute(route: Screen.GameOver, onView: () -> Unit, onAgain: () -> Unit, onMenu: () -> Unit) {
    val model: GameOverViewModel = viewModel(key = "result-${route.eventId}-${route.isEdit}", factory = AppViewModelFactory(route))
    val state by model.state.collectAsStateWithLifecycle()
    RouteLifecycle(model::resume, model::pause)
    GameOverScreen(state, onView, onAgain, onMenu, model::retry)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onView: () -> Unit, onAgain: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    var entered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(true) { entered = true }
    val scale by animateFloatAsState(if (entered) 1f else 0.8f, if (LocalGentleMotion.current) spring(dampingRatio = 0.8f, stiffness = 500f) else snap(), label = "saved")
    val confetti by animateFloatAsState(if (entered) 1f else 0f, tween(if (LocalGentleMotion.current) 650 else 0), label = "confetti")
    ScreenBackdrop(result = true) {
        PageColumn(Modifier.widthIn(max = 600.dp).align(Alignment.TopCenter)) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val palette = listOf(CelebrationGreen, CelebrationPink, PaperBorder)
                    repeat(12) { i ->
                        val x = size.width * (i + 0.5f) / 12f
                        val y = (i % 3) * 12.dp.toPx() + confetti * 45.dp.toPx()
                        drawCircle(palette[i % 3].copy(alpha = 0.8f), 3.dp.toPx(), Offset(x, y))
                    }
                }
                Surface(Modifier.size(64.dp).graphicsLayer { scaleX = scale; scaleY = scale }, shape = CircleShape, color = Paper, contentColor = ForestBackground, shadowElevation = 6.dp) {
                    Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.confirm_mark), style = MaterialTheme.typography.displaySmall) }
                }
            }
            Text(stringResource(if (state.isEdit) R.string.updated_title else R.string.saved_title), style = MaterialTheme.typography.headlineMedium, color = LightText, textAlign = TextAlign.Center)
            Text(stringResource(R.string.saved_subtitle), style = MaterialTheme.typography.bodyLarge, color = PaperBorder,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (state.loading) CircularProgressIndicator(color = CelebrationGreen)
            state.card?.let { EventPictureCard(it, state.style) }
            if (!state.loading && state.card == null) InlineStatus(R.string.missing_card)
            if (state.problem != null) InlineStatus(R.string.read_error, true, onRetry)
            Spacer(Modifier.height(8.dp))
            PrimaryButton(R.string.view_card, onView, enabled = state.card != null)
            SecondaryButton(R.string.create_again, onAgain, Modifier.fillMaxWidth())
            OutlinedButton(onMenu, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Image(painterResource(R.drawable.icon_home), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.menu), color = LightText)
            }
        }
    }
}
