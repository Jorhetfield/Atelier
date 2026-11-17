package com.jorhetfield.atelier.screenshottests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.jorhetfield.atelier.designsystem.preview.AtelierPreviews
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme

@PreviewTest
@AtelierPreviews
@Composable
fun PaletteScreenshot() {
    ScreenshotSurface {
        val scheme = MaterialTheme.colorScheme
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AtelierTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AtelierTheme.spacing.sm),
        ) {
            Swatch("primary", scheme.primary)
            Swatch("primaryCont", scheme.primaryContainer)
            Swatch("secondary", scheme.secondary)
            Swatch("secondaryCont", scheme.secondaryContainer)
            Swatch("tertiary", scheme.tertiary)
            Swatch("tertiaryCont", scheme.tertiaryContainer)
            Swatch("error", scheme.error)
            Swatch("errorCont", scheme.errorContainer)
            Swatch("surface", scheme.surface)
            Swatch("surfLow", scheme.surfaceContainerLow)
            Swatch("surfHigh", scheme.surfaceContainerHigh)
            Swatch("surfHighest", scheme.surfaceContainerHighest)
            Swatch("outline", scheme.outline)
            Swatch("outlineVar", scheme.outlineVariant)
        }
    }
}

@PreviewTest
@AtelierPreviews
@Composable
fun TypographyScreenshot() {
    ScreenshotSurface {
        Column(verticalArrangement = Arrangement.spacedBy(AtelierTheme.spacing.sm)) {
            Text("Display Small", style = MaterialTheme.typography.displaySmall)
            Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
            Text("Title Large", style = MaterialTheme.typography.titleLarge)
            Text("Title Medium", style = MaterialTheme.typography.titleMedium)
            Text("Body Large", style = MaterialTheme.typography.bodyLarge)
            Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
            Text("Label Medium", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Swatch(
    name: String,
    color: Color,
) {
    Column(modifier = Modifier.width(SwatchSize)) {
        Box(
            modifier =
                Modifier
                    .size(SwatchSize)
                    .background(color, RoundedCornerShape(AtelierTheme.spacing.md)),
        )
        Text(text = name, style = MaterialTheme.typography.labelSmall)
    }
}

private val SwatchSize = 72.dp
