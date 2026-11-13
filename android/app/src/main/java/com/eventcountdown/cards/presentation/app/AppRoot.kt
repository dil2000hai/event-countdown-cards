package com.eventcountdown.cards.presentation.app

import android.app.Activity
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.core.theme.LocalGentleMotion
import com.eventcountdown.cards.presentation.about.AboutRoute
import com.eventcountdown.cards.presentation.detail.EventDetailRoute
import com.eventcountdown.cards.presentation.game.GameRoute
import com.eventcountdown.cards.presentation.gameover.GameOverRoute
import com.eventcountdown.cards.presentation.menu.MenuRoute
import com.eventcountdown.cards.presentation.past.PastEventsRoute
import com.eventcountdown.cards.presentation.settings.SettingsRoute
import com.eventcountdown.cards.presentation.splash.SplashRoute

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = AppViewModelFactory())
    val screen by model.screen.collectAsStateWithLifecycle()
    val navigation by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lightBars = screen is Screen.Game || screen == Screen.PastEvents || screen == Screen.About
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = lightBars
                isAppearanceLightNavigationBars = lightBars
            }
        }
    }
    BackHandler {
        if (screen == Screen.Menu || screen == Screen.Splash || navigation.backStack.isEmpty()) (context as? Activity)?.finish() else model.back()
    }
    CompositionLocalProvider(LocalGentleMotion provides navigation.animationsEnabled) {
        AnimatedContent(targetState = screen, transitionSpec = { fadeIn(tween(if (navigation.animationsEnabled) 300 else 0)) togetherWith fadeOut(tween(if (navigation.animationsEnabled) 180 else 0)) }, label = "route") { target ->
            when (target) {
                Screen.Splash -> SplashRoute(model::finishSplash)
                Screen.Menu -> MenuRoute({ model.navigate(Screen.Game()) }, { model.navigate(Screen.EventDetail(it)) },
                    { model.navigate(Screen.PastEvents) }, { model.navigate(Screen.Settings) }, { model.navigate(Screen.About) })
                is Screen.Game -> GameRoute(target, model::back) { saved ->
                    if (saved.haptic && Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    model.saved(saved.id, saved.isEdit)
                }
                is Screen.GameOver -> GameOverRoute(target, { model.navigate(Screen.EventDetail(target.eventId)) }, { model.navigate(Screen.Game(fresh = true)) }, model::menu)
                is Screen.EventDetail -> EventDetailRoute(target, model::back, model::menu, { model.navigate(Screen.Game(eventId = target.eventId)) },
                    { model.navigate(Screen.Game(eventId = target.eventId, replan = true, fresh = true)) }, model::returnToList)
                Screen.PastEvents -> PastEventsRoute(model::back, model::menu, { model.navigate(Screen.EventDetail(it)) })
                Screen.Settings -> SettingsRoute(model::back, model::menu)
                Screen.About -> AboutRoute(model::back, model::menu)
            }
        }
    }
}
