package com.eventcountdown.cards.domain.model

interface AppInfoSource {
    fun version(): String?
    fun packageName(): String
}
