package com.eventcountdown.cards.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.eventcountdown.cards.R
import com.eventcountdown.cards.core.theme.*
import com.eventcountdown.cards.domain.model.*
import com.eventcountdown.cards.domain.usecase.Countdown
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

data class EventCardModel(val event: Event, val countdown: Countdown)

fun pictureResource(picture: Picture): Int = when (picture) {
    Picture.Birthday -> R.drawable.sprite_1
    Picture.Winter -> R.drawable.sprite_2
    Picture.Garden -> R.drawable.sprite_3
    Picture.Mountains -> R.drawable.sprite_4
}
fun pictureLabel(picture: Picture): Int = when (picture) {
    Picture.Birthday -> R.string.picture_birthday
    Picture.Winter -> R.string.picture_winter
    Picture.Garden -> R.string.picture_garden
    Picture.Mountains -> R.string.picture_mountains
}
fun categoryLabel(category: Category): Int = when (category) {
    Category.Birthday -> R.string.birthday
    Category.Holiday -> R.string.holiday
    Category.Trip -> R.string.trip
    Category.Other -> R.string.other
}

@Composable
fun dateLabel(date: LocalDate): String = stringResource(R.string.date_format, date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))

@Composable
fun countdownLabel(countdown: Countdown): String = when {
    countdown.calendarDays == 0L -> stringResource(R.string.today)
    countdown.calendarDays > 0L -> pluralStringResource(R.plurals.days_left, countdown.calendarDays.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), countdown.calendarDays)
    else -> pluralStringResource(R.plurals.days_ago, abs(countdown.calendarDays).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), abs(countdown.calendarDays))
}

@Composable
fun EventPictureCard(model: EventCardModel, style: CardStyle, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, onPin: (() -> Unit)? = null, pinEnabled: Boolean = true) {
    val description = stringResource(R.string.card_accessibility, model.event.name, countdownLabel(model.countdown), dateLabel(model.event.eventDate))
    val content: @Composable () -> Unit = {
        Column {
            Box(Modifier.fillMaxWidth().heightIn(min = 128.dp).background(Brush.horizontalGradient(listOf(ForestSurface, ForestBackground)))) {
                Crossfade(model.event.picture, Modifier.size(132.dp).align(Alignment.CenterEnd), animationSpec = tween(if (LocalGentleMotion.current) 180 else 0), label = "picture") { picture ->
                    Image(painterResource(pictureResource(picture)), stringResource(pictureLabel(picture)), Modifier.size(132.dp).padding(6.dp), contentScale = ContentScale.Fit)
                }
                Column(Modifier.padding(18.dp).padding(end = 96.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(categoryLabel(model.event.category)), style = MaterialTheme.typography.labelSmall, color = PaperBorder)
                    Crossfade(countdownLabel(model.countdown), animationSpec = tween(if (LocalGentleMotion.current) 160 else 0), label = "day") { label -> Text(label, style = MaterialTheme.typography.headlineLarge, color = LightText) }
                    if (model.countdown.isArchived) Text(stringResource(R.string.archived_badge), style = MaterialTheme.typography.labelSmall, color = PaperBorder)
                }
            }
            Column(Modifier.fillMaxWidth().background(if (style == CardStyle.Cream) Paper else ForestSurface).padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val textColor = if (style == CardStyle.Cream) ForestBackground else LightText
                Text(model.event.name, style = MaterialTheme.typography.titleLarge, color = textColor)
                Text(dateLabel(model.event.eventDate), style = MaterialTheme.typography.bodyMedium, color = textColor)
                if (model.event.pinnedInApp) Text(stringResource(R.string.pinned_badge), style = MaterialTheme.typography.labelSmall, color = textColor)
                if (onPin != null) SecondaryButton(if (model.event.pinnedInApp) R.string.unpin else R.string.pin, onPin, enabled = pinEnabled, light = style == CardStyle.Cream)
            }
        }
    }
    val pinState = stringResource(R.string.pin_status, stringResource(if (model.event.pinnedInApp) R.string.on_state else R.string.off_state))
    val cardModifier = modifier.fillMaxWidth().semantics { contentDescription = description; stateDescription = pinState }
    if (onClick != null) Card(onClick, cardModifier, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.4f)), elevation = CardDefaults.cardElevation(6.dp), content = { content() })
    else Card(cardModifier, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.4f)), elevation = CardDefaults.cardElevation(6.dp), content = { content() })
}
