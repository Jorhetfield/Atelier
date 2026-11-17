package com.jorhetfield.atelier.ui.model

import androidx.compose.ui.graphics.Color
import com.jorhetfield.atelier.domain.model.DominantColor

private const val MIN_TINT_SATURATION = 0.3f

// Many artworks have a dominant color close to grey, which would tint nothing. Keeping the hue and
// guaranteeing some saturation gives a page that still feels like the picture.
fun DominantColor.toTint(): Color =
    Color.hsl(
        hue = hue.coerceIn(0, 360).toFloat(),
        saturation = (saturation / 100f).coerceIn(MIN_TINT_SATURATION, 1f),
        lightness = (lightness / 100f).coerceIn(0f, 1f),
    )

fun DominantColor.toColor(): Color =
    Color.hsl(
        hue = hue.coerceIn(0, 360).toFloat(),
        saturation = (saturation / 100f).coerceIn(0f, 1f),
        lightness = (lightness / 100f).coerceIn(0f, 1f),
    )
