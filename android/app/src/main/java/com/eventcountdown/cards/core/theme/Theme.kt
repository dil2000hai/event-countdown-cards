package com.eventcountdown.cards.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = CelebrationGreen, onPrimary = ForestBackground, secondary = ForestSurface, onSecondary = LightText,
    tertiary = CelebrationPink, background = ForestBackground, onBackground = LightText,
    surface = ForestSurface, onSurface = LightText, surfaceVariant = ForestBackground, onSurfaceVariant = LightText,
    error = PaperBorder, onError = ForestBackground, outline = PaperBorder.copy(alpha = 0.45f)
)
private val LightColors = lightColorScheme(
    primary = ForestSurface, onPrimary = Paper, secondary = CelebrationGreen, onSecondary = ForestBackground,
    tertiary = CelebrationPink, background = Paper, onBackground = ForestBackground,
    surface = Paper, onSurface = ForestBackground, surfaceVariant = PaperRaised, onSurfaceVariant = ForestSurface,
    error = ForestBackground, onError = Paper, outline = ForestSurface
)

@Composable
fun AppTheme(light: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (light) LightColors else DarkColors, typography = AppTypography, content = content)
}
