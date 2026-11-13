package com.eventcountdown.cards.data.about

import android.content.Context
import com.eventcountdown.cards.domain.model.AppInfoSource

class AppInfoProvider(private val context: Context) : AppInfoSource {
    override fun version(): String? = try { context.packageManager.getPackageInfo(context.packageName, 0).versionName } catch (e: Exception) { null }
    override fun packageName(): String = context.packageName
}
