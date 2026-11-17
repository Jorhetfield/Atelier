package com.jorhetfield.atelier.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jorhetfield.atelier.ui.model.ArtworkImageUi
import com.jorhetfield.atelier.ui.model.toColor
import com.jorhetfield.atelier.ui.model.url

// The width the detail hero asks for; the viewer reuses that same url as its instant first layer.
const val DETAIL_IMAGE_WIDTH = 1200

// Three layers: a wash of the artwork's own dominant color, its tiny blurred preview, and the real
// picture fading in on top, so there is never an empty frame while the full image downloads.
@Composable
fun ArtworkImage(
    image: ArtworkImageUi?,
    width: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(modifier = modifier.fillMaxSize().background(placeholderBrush(image))) {
        if (image == null) return@Box

        image.placeholder?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(image.url(width))
                    .crossfade(true)
                    .build(),
            contentDescription = image.contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun placeholderBrush(image: ArtworkImageUi?): Brush {
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest
    val tint = image?.dominantColor?.toColor()
    val start: Color
    val end: Color
    if (tint != null) {
        start = lerp(surface, tint, 0.35f)
        end = lerp(surface, tint, 0.6f)
    } else {
        start = MaterialTheme.colorScheme.tertiaryContainer
        end = MaterialTheme.colorScheme.primaryContainer
    }
    return Brush.linearGradient(listOf(start, end))
}
