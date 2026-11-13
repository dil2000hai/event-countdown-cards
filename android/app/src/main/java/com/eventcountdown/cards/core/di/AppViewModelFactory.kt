package com.eventcountdown.cards.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eventcountdown.cards.core.navigation.Screen
import com.eventcountdown.cards.presentation.about.AboutViewModel
import com.eventcountdown.cards.presentation.app.AppViewModel
import com.eventcountdown.cards.presentation.detail.EventDetailViewModel
import com.eventcountdown.cards.presentation.game.GameViewModel
import com.eventcountdown.cards.presentation.gameover.GameOverViewModel
import com.eventcountdown.cards.presentation.menu.MenuViewModel
import com.eventcountdown.cards.presentation.past.PastEventsViewModel
import com.eventcountdown.cards.presentation.settings.SettingsViewModel
import com.eventcountdown.cards.presentation.splash.SplashViewModel

class AppViewModelFactory(private val screen: Screen = Screen.Menu) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val d = ServiceLocator.get()
        val model: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel(d.repository, d.systemMotion)
            SplashViewModel::class.java -> SplashViewModel(d.repository)
            MenuViewModel::class.java -> MenuViewModel(d.repository, d.countdown)
            GameViewModel::class.java -> GameViewModel(d.repository, d.clock, d.countdown, screen as Screen.Game, d.defaultName)
            GameOverViewModel::class.java -> (screen as Screen.GameOver).let { GameOverViewModel(d.repository, d.countdown, it.eventId, it.isEdit) }
            EventDetailViewModel::class.java -> EventDetailViewModel(d.repository, d.countdown, (screen as Screen.EventDetail).eventId)
            PastEventsViewModel::class.java -> PastEventsViewModel(d.repository, d.countdown, d.clock)
            SettingsViewModel::class.java -> SettingsViewModel(d.repository, d.countdown)
            AboutViewModel::class.java -> AboutViewModel(d.info)
            else -> error(modelClass.name)
        }
        return model as T
    }
}
