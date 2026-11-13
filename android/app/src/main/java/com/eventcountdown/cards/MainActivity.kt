package com.eventcountdown.cards

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eventcountdown.cards.core.di.ServiceLocator
import com.eventcountdown.cards.core.theme.AppTheme
import com.eventcountdown.cards.presentation.app.AppRoot

class MainActivity : ComponentActivity() {
    private val clockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { ServiceLocator.refreshClock() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent { AppTheme { AppRoot() } }
    }
    override fun onStart() {
        super.onStart()
        registerReceiver(clockReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        })
        ServiceLocator.refreshClock()
    }
    override fun onStop() {
        unregisterReceiver(clockReceiver)
        super.onStop()
    }
}
