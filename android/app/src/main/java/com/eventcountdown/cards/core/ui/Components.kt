package com.eventcountdown.cards.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.theme.*

@Composable
fun ScreenBackdrop(light: Boolean = false, splash: Boolean = false, result: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val colors = when {
            splash -> listOf(ForestBackground.copy(alpha = 0.97f), ForestSurface.copy(alpha = 0.90f), ForestBackground.copy(alpha = 0.98f))
            light -> listOf(Paper.copy(alpha = 0.96f), PaperRaised.copy(alpha = 0.94f), PaperBorder.copy(alpha = 0.98f))
            result -> listOf(ForestSurface.copy(alpha = 0.92f), ForestBackground.copy(alpha = 0.84f), ForestBackground)
            else -> listOf(ForestBackground.copy(alpha = 0.94f), ForestSurface.copy(alpha = 0.78f), ForestBackground.copy(alpha = 0.97f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        Box(Modifier.size(300.dp).align(Alignment.TopEnd).background(Brush.radialGradient(listOf(CelebrationGreen.copy(alpha = 0.13f), Color.Transparent))))
        content()
    }
}

@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
}

@Composable
fun PrimaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && LocalGentleMotion.current) 0.98f else 1f, if (LocalGentleMotion.current) spring(dampingRatio = 0.8f, stiffness = 500f) else snap(), label = "press")
    val shape = RoundedCornerShape(18.dp)
    Button(onClick = onClick, enabled = enabled, interactionSource = interaction,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (enabled) 6.dp else 0.dp, shape).clip(shape)
            .background(if (enabled) Brush.horizontalGradient(listOf(CelebrationPink, ForestSurface, ForestSurface, CelebrationPink)) else Brush.horizontalGradient(listOf(ForestSurface, ForestSurface))),
        shape = shape, border = BorderStroke(1.dp, if (enabled) CelebrationPink else PaperBorder.copy(alpha = 0.3f)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = LightText, disabledContainerColor = ForestBackground.copy(alpha = 0.35f), disabledContentColor = PaperBorder),
        contentPadding = PaddingValues(16.dp)) {
        if (loading) { CircularProgressIndicator(Modifier.size(22.dp), color = LightText, strokeWidth = 2.dp); Spacer(Modifier.width(12.dp)) }
        Text(stringResource(text), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, light: Boolean = false) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp).shadow(if (enabled) 2.dp else 0.dp, RoundedCornerShape(14.dp)), enabled = enabled, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (light) ForestSurface else LightText.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (light) PaperRaised else ForestSurface, contentColor = if (light) ForestBackground else LightText)) {
        Text(stringResource(text), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun RaisedPanel(modifier: Modifier = Modifier, light: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = if (light) Paper.copy(alpha = 0.98f) else ForestSurface.copy(alpha = 0.94f),
        contentColor = if (light) ForestBackground else LightText, border = BorderStroke(1.dp, if (light) PaperBorder else LightText.copy(alpha = 0.16f)), shadowElevation = 4.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun FeatureHeader(title: Int, onBack: () -> Unit, light: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SecondaryButton(R.string.back, onBack, light = light)
        Spacer(Modifier.width(14.dp))
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, color = if (light) ForestBackground else LightText, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ChoiceChip(label: Int, selected: Boolean, onSelect: () -> Unit, light: Boolean = false, enabled: Boolean = true) {
    val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
    FilterChip(selected, onSelect, label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) }, enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = description },
        colors = FilterChipDefaults.filterChipColors(containerColor = if (light) Paper else ForestSurface,
            labelColor = if (light) ForestBackground else LightText, selectedContainerColor = ForestSurface, selectedLabelColor = LightText),
        border = FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected, borderColor = PaperBorder, selectedBorderColor = CelebrationPink, selectedBorderWidth = 3.dp))
}

@Composable
fun InlineStatus(message: Int, error: Boolean = false, onRetry: (() -> Unit)? = null) {
    RaisedPanel(light = true, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(stringResource(message), style = if (error) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium)
        if (onRetry != null) SecondaryButton(R.string.retry, onRetry, light = true)
    }
}

@Composable
fun ConfirmationDialog(title: Int, message: String, confirm: Int, busy: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(stringResource(title)) }, text = { Text(message) },
        confirmButton = { TextButton(onConfirm, enabled = !busy) { Text(stringResource(confirm)) } },
        dismissButton = { TextButton(onDismiss, enabled = !busy) { Text(stringResource(R.string.cancel)) } })
}
