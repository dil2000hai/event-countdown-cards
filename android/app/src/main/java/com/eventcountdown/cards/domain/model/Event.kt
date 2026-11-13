package com.eventcountdown.cards.domain.model

import java.time.LocalDate

enum class Category { Birthday, Holiday, Trip, Other }
enum class Picture { Birthday, Winter, Garden, Mountains }
enum class CardStyle { Cream, Forest }
enum class DraftMode { Create, Edit, Replan }

data class Event(
    val id: String,
    val name: String,
    val category: Category,
    val eventDate: LocalDate,
    val picture: Picture,
    val notes: String,
    val pinnedInApp: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val manuallyArchivedAt: Long? = null
)

data class EventDraft(
    val draftId: String,
    val mode: DraftMode,
    val sourceEventId: String?,
    val name: String,
    val category: Category,
    val eventDate: LocalDate,
    val picture: Picture,
    val notes: String = "",
    val pinnedInApp: Boolean = false,
    val updatedAt: Long = 0
)

data class AppSettings(
    val cardStyle: CardStyle = CardStyle.Cream,
    val gentleAnimationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

enum class StorageProblem { Read, Write, NewerSchema, Missing }

data class CollectionState(
    val loading: Boolean = true,
    val events: List<Event> = emptyList(),
    val draft: EventDraft? = null,
    val settings: AppSettings = AppSettings(),
    val problem: StorageProblem? = null,
    val imageFallback: Boolean = false,
    val revision: Long = 0
)
