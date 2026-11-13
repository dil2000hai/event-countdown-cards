package com.eventcountdown.cards.data.repository

import com.eventcountdown.cards.data.local.CountdownPreferences
import com.eventcountdown.cards.data.local.UnsupportedSchema
import com.eventcountdown.cards.data.sample.SampleCopy
import com.eventcountdown.cards.data.sample.SampleData
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.repository.CountdownRepository
import com.eventcountdown.cards.domain.time.TimeSource
import com.eventcountdown.cards.domain.usecase.ValidateEvent
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CountdownRepositoryImpl(
    private val preferences: CountdownPreferences,
    private val clock: TimeSource,
    private val sampleCopy: SampleCopy
) : CountdownRepository {
    private val mutableState = MutableStateFlow(CollectionState())
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private val generation = AtomicLong()
    private var initialized = false
    private var writable = false

    override suspend fun load(force: Boolean) = mutex.withLock {
        if (initialized && !force) return@withLock
        mutableState.update { it.copy(loading = true, problem = null) }
        try {
            val stored = withContext(Dispatchers.IO) { preferences.read() }
            val events = if (stored.seeded) stored.events else SampleData.create(clock.today(), clock.now().toEpochMilli(), sampleCopy)
            if (!stored.seeded) {
                val committed = withContext(Dispatchers.IO) { preferences.write(events, null, stored.settings) }
                if (!committed) throw IllegalStateException()
            }
            writable = true
            initialized = true
            mutableState.update { it.copy(loading = false, events = events.toList(), draft = stored.draft, settings = stored.settings, problem = null, imageFallback = stored.fallback) }
        } catch (e: Exception) {
            writable = false
            initialized = true
            mutableState.update { it.copy(loading = false, problem = if (e is UnsupportedSchema) StorageProblem.NewerSchema else StorageProblem.Read) }
        }
    }

    override fun refreshClock() { mutableState.update { it.copy(revision = it.revision + 1) } }
    override fun newDraftGeneration(): Long = generation.incrementAndGet()
    override suspend fun saveDraft(draft: EventDraft, generation: Long): Boolean = mutex.withLock {
        if (generation != this.generation.get()) return@withLock true
        commit(mutableState.value.copy(draft = draft))
    }
    override suspend fun clearDraft(): Boolean {
        generation.incrementAndGet()
        return mutex.withLock { commit(mutableState.value.copy(draft = null)) }
    }
    override suspend fun saveEvent(draft: EventDraft): String? {
        generation.incrementAndGet()
        return mutex.withLock {
            if (!ValidateEvent()(draft, clock.today()).valid) return@withLock null
            val previous = draft.sourceEventId?.let { id -> mutableState.value.events.find { it.id == id } }
            if (draft.mode == DraftMode.Edit && previous == null) {
                mutableState.update { it.copy(problem = StorageProblem.Missing) }
                return@withLock null
            }
            val now = clock.now().toEpochMilli()
            val id = previous?.id ?: draft.draftId
            val event = Event(id, draft.name.trim(), draft.category, draft.eventDate, draft.picture, draft.notes,
                draft.pinnedInApp, previous?.createdAt ?: now, now, previous?.manuallyArchivedAt)
            val events = mutableState.value.events.filterNot { it.id == id } + event
            if (commit(mutableState.value.copy(events = events, draft = null))) id else null
        }
    }
    override suspend fun togglePin(id: String): Boolean = change(id) { it.copy(pinnedInApp = !it.pinnedInApp) }
    override suspend fun archive(id: String, archived: Boolean): Boolean = change(id) {
        it.copy(manuallyArchivedAt = if (archived) clock.now().toEpochMilli() else null)
    }
    override suspend fun delete(id: String): Boolean = mutex.withLock {
        if (mutableState.value.events.none { it.id == id }) return@withLock false
        val current = mutableState.value
        commit(current.copy(events = current.events.filterNot { it.id == id }, draft = current.draft?.takeUnless { it.sourceEventId == id }))
    }
    override suspend fun saveSettings(settings: AppSettings): Boolean = mutex.withLock { commit(mutableState.value.copy(settings = settings)) }
    private suspend fun change(id: String, transform: (Event) -> Event): Boolean = mutex.withLock {
        if (mutableState.value.events.none { it.id == id }) return@withLock false
        commit(mutableState.value.copy(events = mutableState.value.events.map { if (it.id == id) transform(it).copy(updatedAt = clock.now().toEpochMilli()) else it }))
    }
    private suspend fun commit(next: CollectionState): Boolean {
        if (!writable) return false
        val success = withContext(Dispatchers.IO) { try { preferences.write(next.events, next.draft, next.settings) } catch (e: Exception) { false } }
        if (success) mutableState.value = next.copy(problem = null) else mutableState.update { it.copy(problem = StorageProblem.Write) }
        return success
    }
}
