package com.eventcountdown.cards.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.ScreenBackdrop

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val opening = stringResource(R.string.opening)
    ScreenBackdrop(splash = true) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.collection_label), style = MaterialTheme.typography.labelSmall, color = PaperBorder)
            Spacer(Modifier.height(36.dp))
            Box(Modifier.size(252.dp).background(Brush.radialGradient(listOf(CelebrationGreen.copy(alpha = 0.22f), ForestBackground.copy(alpha = 0f))), CircleShape), contentAlignment = Alignment.Center) {
                if (state.animationsEnabled && LocalGentleMotion.current) FloatingEmblem() else Image(painterResource(R.drawable.logo), null, Modifier.size(224.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = LightText, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.splash_subtitle), color = LightText.copy(alpha = 0.86f), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            LinearProgressIndicator(Modifier.width(132.dp).semantics { contentDescription = opening }, color = CelebrationGreen, trackColor = LightText.copy(alpha = 0.18f))
            Spacer(Modifier.height(30.dp))
            Image(painterResource(R.drawable.sprite_5), null, Modifier.size(52.dp).graphicsLayer { alpha = 0.65f }, contentScale = ContentScale.Fit)
        }
    }
}

@Composable
private fun FloatingEmblem() {
    val transition = rememberInfiniteTransition(label = "celebration")
    val float by transition.animateFloat(-6f, 6f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "float")
    val turn by transition.animateFloat(-8f, 8f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "turn")
    Image(painterResource(R.drawable.logo), null, Modifier.size(224.dp).graphicsLayer { translationY = float.dp.toPx(); rotationZ = turn / 3 }, contentScale = ContentScale.Fit)
}
