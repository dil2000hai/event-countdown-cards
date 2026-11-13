package com.eventcountdown.cards.domain.repository

import com.eventcountdown.cards.domain.model.AppSettings
import com.eventcountdown.cards.domain.model.CollectionState
import com.eventcountdown.cards.domain.model.EventDraft
import kotlinx.coroutines.flow.StateFlow

interface CountdownRepository {
    val state: StateFlow<CollectionState>
    suspend fun load(force: Boolean = false)
    fun refreshClock()
    fun newDraftGeneration(): Long
    suspend fun saveDraft(draft: EventDraft, generation: Long): Boolean
    suspend fun clearDraft(): Boolean
    suspend fun saveEvent(draft: EventDraft): String?
    suspend fun togglePin(id: String): Boolean
    suspend fun archive(id: String, archived: Boolean): Boolean
    suspend fun delete(id: String): Boolean
    suspend fun saveSettings(settings: AppSettings): Boolean
}
