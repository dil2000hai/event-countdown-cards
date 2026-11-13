package com.eventcountdown.cards.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.domain.repository.CountdownRepository
import kotlinx.coroutines.launch
import com.eventcountdown.cards.core.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class AppViewModel(repository: CountdownRepository, systemMotion: () -> Boolean) : ViewModel() {
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = mutableScreen.asStateFlow()
    private val mutableState = MutableStateFlow(AppUiState())
    val state = mutableState.asStateFlow()
    init { viewModelScope.launch { repository.state.collect { data -> mutableState.update { it.copy(animationsEnabled = data.settings.gentleAnimationsEnabled && systemMotion()) } } } }
    fun finishSplash() {
        if (mutableScreen.value != Screen.Splash || mutableState.value.splashConsumed) return
        mutableState.update { it.copy(splashConsumed = true) }
        mutableScreen.value = Screen.Menu
    }
    fun navigate(screen: Screen) {
        val current = mutableScreen.value
        if (current == screen) return
        if (current != Screen.Splash) mutableState.update { it.copy(backStack = it.backStack + current) }
        mutableScreen.value = screen
    }
    fun saved(id: String, edit: Boolean) { navigate(Screen.GameOver(id, edit)) }
    fun back() {
        val previous = mutableState.value.backStack.lastOrNull() ?: return
        mutableState.update { it.copy(backStack = it.backStack.dropLast(1)) }
        mutableScreen.value = previous
    }
    fun menu() {
        mutableState.update { it.copy(backStack = emptyList()) }
        mutableScreen.value = Screen.Menu
    }
    fun returnToList() {
        val stack = mutableState.value.backStack
        val index = stack.indexOfLast { it == Screen.Menu || it == Screen.PastEvents }
        if (index < 0) menu() else {
            mutableScreen.value = stack[index]
            mutableState.update { it.copy(backStack = stack.take(index)) }
        }
    }
}
