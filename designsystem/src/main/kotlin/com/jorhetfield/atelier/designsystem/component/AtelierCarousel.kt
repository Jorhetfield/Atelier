package com.jorhetfield.atelier.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme

private const val TEXT_FADE_START = 0.7f
private val CarouselHeight = 280.dp
private val PreferredItemWidth = 232.dp

// Multi-browse: one large item and a couple of smaller ones peeking in, so the row invites scrolling.
@Composable
public fun AtelierCarousel(
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable CarouselItemScope.(index: Int) -> Unit,
) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { itemCount },
        preferredItemWidth = PreferredItemWidth,
        modifier = modifier.fillMaxWidth().height(CarouselHeight),
        itemSpacing = AtelierTheme.spacing.sm,
        contentPadding = PaddingValues(horizontal = AtelierTheme.spacing.lg),
        content = content,
    )
}

@Composable
public fun CarouselItemScope.AtelierCarouselCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick),
    ) {
        image()
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))),
        )
        val drawInfo = carouselItemDrawInfo
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(AtelierTheme.spacing.lg)
                    // The text only belongs on the large item: on the small ones it would be cut mid-word.
                    .graphicsLayer {
                        val range = drawInfo.maxSize - drawInfo.minSize
                        val grown = if (range > 0f) (drawInfo.size - drawInfo.minSize) / range else 1f
                        alpha = ((grown - TEXT_FADE_START) / (1f - TEXT_FADE_START)).coerceIn(0f, 1f)
                    },
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
public fun CarouselItemScope.AtelierCarouselPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}
