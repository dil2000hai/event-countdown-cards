package com.eventcountdown.cards.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.di.AppViewModelFactory
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.core.ui.*
import com.eventcountdown.cards.domain.model.CardStyle

@Composable
fun SettingsRoute(onBack: () -> Unit, onMenu: () -> Unit) {
    val model: SettingsViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    RouteLifecycle(model::resume, model::pause)
    SettingsScreen(state, onBack, onMenu, model::style, model::animations, model::haptics, model::resetDialog, model::reset, model::retrySave)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onMenu: () -> Unit, onStyle: (CardStyle) -> Unit, onAnimations: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit, onResetDialog: (Boolean) -> Unit, onReset: () -> Unit, onRetry: () -> Unit) {
    ScreenBackdrop {
        PageColumn(Modifier.widthIn(max = 640.dp).align(Alignment.TopCenter)) {
            FeatureHeader(R.string.settings_title, onBack)
            Text(stringResource(R.string.style_heading), style = MaterialTheme.typography.titleLarge, color = LightText)
            if (state.loading) CircularProgressIndicator(color = CelebrationGreen)
            if (state.preview != null) EventPictureCard(state.preview, state.settings.cardStyle) else RaisedPanel {
                Image(painterResource(R.drawable.sprite_1), null, Modifier.size(88.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
                Text(stringResource(R.string.style_preview), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.default_name), style = MaterialTheme.typography.titleLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ChoiceChip(R.string.cream, state.settings.cardStyle == CardStyle.Cream, { onStyle(CardStyle.Cream) }, enabled = !state.busy && !state.loading)
                ChoiceChip(R.string.forest, state.settings.cardStyle == CardStyle.Forest, { onStyle(CardStyle.Forest) }, enabled = !state.busy && !state.loading)
            }
            RaisedPanel {
                SettingRow(R.string.gentle_animations, R.string.gentle_hint, state.settings.gentleAnimationsEnabled, !state.busy && !state.loading, onAnimations)
                HorizontalDivider(color = PaperBorder)
                SettingRow(R.string.haptics_title, R.string.haptics_hint, state.settings.hapticsEnabled, !state.busy && !state.loading, onHaptics)
            }
            state.error?.let { InlineStatus(it, true, onRetry) }
            SecondaryButton(R.string.reset_style, { onResetDialog(true) }, Modifier.fillMaxWidth(), !state.busy)
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        }
    }
    if (state.resetVisible) ConfirmationDialog(R.string.reset_title, stringResource(R.string.reset_body), R.string.reset, state.busy, onReset, { onResetDialog(false) })
}

@Composable
private fun SettingRow(title: Int, hint: Int, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val description = stringResource(if (checked) R.string.on_state else R.string.off_state)
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text(stringResource(title), style = MaterialTheme.typography.bodyLarge); Text(stringResource(hint), style = MaterialTheme.typography.bodyMedium) }
        Switch(checked, onChange, Modifier.semantics { stateDescription = description }, enabled = enabled)
    }
}
