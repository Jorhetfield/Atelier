package com.jorhetfield.atelier.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.jorhetfield.atelier.domain.model.DominantColor
import com.jorhetfield.atelier.ui.model.toTint

// The same artwork color tints the card in the list and the page in the detail. Dark surfaces need
// a stronger dose for the hue to read at all.
fun Color.tintedWith(
    dominantColor: DominantColor?,
    lightStrength: Float,
    darkStrength: Float,
): Color {
    val tint = dominantColor?.toTint() ?: return this
    val strength = if (luminance() < 0.5f) darkStrength else lightStrength
    return lerp(this, tint, strength)
}
