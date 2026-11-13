package com.eventcountdown.cards.presentation.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventcountdown.cards.R
import com.eventcountdown.cards.domain.model.AppInfoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class AboutViewModel(info: AppInfoSource) : ViewModel() {
    private val mutableState = MutableStateFlow(AboutUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<Unit>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init { viewModelScope.launch { val version = withContext(Dispatchers.IO) { info.version() }; mutableState.update { it.copy(version = version, packageName = info.packageName()) } } }
    fun privacy() { mutableState.update { it.copy(privacyExpanded = !it.privacyExpanded) } }
    fun contact() { mutableState.update { it.copy(contactExpanded = !it.contactExpanded) } }
    fun copy() { viewModelScope.launch { channel.send(Unit) } }
    fun copied(success: Boolean) { mutableState.update { it.copy(status = if (success) R.string.copied else R.string.copy_error) } }
}
