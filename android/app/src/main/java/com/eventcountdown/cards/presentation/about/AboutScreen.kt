package com.eventcountdown.cards.presentation.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.ui.*

@Composable
fun AboutRoute(onBack: () -> Unit, onMenu: () -> Unit) {
    val model: AboutViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val version = state.version?.let { stringResource(R.string.version, it) } ?: stringResource(R.string.version_unavailable)
    val details = stringResource(R.string.copy_text, appName, version, state.packageName)
    val currentDetails by rememberUpdatedState(details)
    LaunchedEffect(Unit) {
        model.events.collect {
            val copied = try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(appName, currentDetails))
                true
            } catch (e: Exception) { false }
            model.copied(copied)
        }
    }
    AboutScreen(state, version, onBack, onMenu, model::privacy, model::contact, model::copy)
}

@Composable
fun AboutScreen(state: AboutUiState, version: String, onBack: () -> Unit, onMenu: () -> Unit, onPrivacy: () -> Unit, onContact: () -> Unit, onCopy: () -> Unit) {
    com.eventcountdown.cards.core.theme.AppTheme(light = true) {
        ScreenBackdrop(light = true) {
            PageColumn(Modifier.widthIn(max = 640.dp).align(Alignment.TopCenter)) {
                FeatureHeader(R.string.about_title, onBack, true)
                RaisedPanel {
                    Image(painterResource(R.drawable.logo), null, Modifier.size(88.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                    Text(version, style = MaterialTheme.typography.bodyMedium)
                    Text(state.packageName, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.about_tagline))
                }
                RaisedPanel {
                    val expanded = stringResource(if (state.privacyExpanded) R.string.expanded else R.string.collapsed)
                    TextButton(onPrivacy, Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = expanded }) { Text(stringResource(R.string.privacy_policy)) }
                    if (state.privacyExpanded) Text(stringResource(R.string.privacy_body))
                }
                RaisedPanel {
                    val expanded = stringResource(if (state.contactExpanded) R.string.expanded else R.string.collapsed)
                    TextButton(onContact, Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = expanded }) { Text(stringResource(R.string.contact)) }
                    if (state.contactExpanded) {
                        Text(stringResource(R.string.contact_body))
                        PrimaryButton(R.string.copy_details, onCopy)
                    }
                }
                state.status?.let { InlineStatus(it) }
                SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), light = true)
                Text(stringResource(R.string.offline_footer), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
