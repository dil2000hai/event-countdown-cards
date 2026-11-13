package com.eventcountdown.cards.core.navigation

import java.util.UUID

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val sessionId: String = UUID.randomUUID().toString(), val eventId: String? = null, val replan: Boolean = false, val fresh: Boolean = false) : Screen
    data class GameOver(val eventId: String, val isEdit: Boolean) : Screen
    data class EventDetail(val eventId: String) : Screen
    data object PastEvents : Screen
    data object Settings : Screen
    data object About : Screen
}
