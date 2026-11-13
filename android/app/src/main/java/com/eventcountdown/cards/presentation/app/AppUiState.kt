package com.eventcountdown.cards.presentation.app

import com.eventcountdown.cards.core.navigation.Screen

data class AppUiState(val backStack: List<Screen> = emptyList(), val splashConsumed: Boolean = false, val animationsEnabled: Boolean = true)
