package com.jorhetfield.atelier.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.jorhetfield.atelier.designsystem.component.ArtworkCard
import com.jorhetfield.atelier.designsystem.component.AtelierDetailRow
import com.jorhetfield.atelier.designsystem.component.AtelierEmptyState
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierSearchField
import com.jorhetfield.atelier.designsystem.preview.AtelierFontScalePreviews
import com.jorhetfield.atelier.designsystem.preview.AtelierPreviews
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme

@PreviewTest
@AtelierPreviews
@Composable
fun SearchFieldScreenshot() {
    ScreenshotSurface {
        Column(verticalArrangement = Arrangement.spacedBy(AtelierTheme.spacing.md)) {
            AtelierSearchField(
                value = "",
                onValueChange = {},
                placeholder = "Search artworks",
                clearContentDescription = "Clear",
            )
            AtelierSearchField(
                value = "water lilies",
                onValueChange = {},
                placeholder = "Search artworks",
                clearContentDescription = "Clear",
            )
        }
    }
}

@PreviewTest
@AtelierPreviews
@AtelierFontScalePreviews
@Composable
fun ArtworkCardScreenshot() {
    ScreenshotSurface {
        Column(verticalArrangement = Arrangement.spacedBy(AtelierTheme.spacing.lg)) {
            ArtworkCard(
                title = "A Sunday on La Grande Jatte",
                subtitle = "Georges Seurat, 1884",
                onClick = {},
                image = { SampleImage() },
            )
            ArtworkCard(
                title = "Self-Portrait with a Very Long Title That Needs to Wrap Over Two Lines",
                subtitle = null,
                onClick = {},
                image = { SampleImage() },
            )
        }
    }
}

@PreviewTest
@AtelierPreviews
@Composable
fun EmptyStateScreenshot() {
    ScreenshotSurface {
        AtelierEmptyState(
            title = "Nothing here yet",
            message = "Search for an artist, a movement or a title to start exploring the collection.",
        )
    }
}

@PreviewTest
@AtelierPreviews
@Composable
fun ErrorStateScreenshot() {
    ScreenshotSurface {
        AtelierErrorState(
            title = "Can't reach the gallery",
            message = "Check your connection and try again.",
            retryLabel = "Try again",
            onRetry = {},
        )
    }
}

@PreviewTest
@AtelierPreviews
@AtelierFontScalePreviews
@Composable
fun DetailRowsScreenshot() {
    ScreenshotSurface {
        Column {
            AtelierDetailRow(label = "Artist", value = "Georges Seurat")
            AtelierDetailRow(label = "Date", value = "1884-86")
            AtelierDetailRow(label = "Medium", value = "Oil on canvas")
            AtelierDetailRow(label = "Origin", value = "France")
        }
    }
}
