package com.jorhetfield.atelier.ui.viewer

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jorhetfield.atelier.ui.components.DETAIL_IMAGE_WIDTH
import com.jorhetfield.atelier.ui.model.ArtworkImageUi
import com.jorhetfield.atelier.ui.model.url
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val VIEWER_IMAGE_WIDTH = 1686

@Composable
fun ZoomableArtwork(
    image: ArtworkImageUi,
    modifier: Modifier = Modifier,
) {
    var container by remember { mutableStateOf(Size.Zero) }
    var transform by remember { mutableStateOf(ZoomTransform()) }
    var running by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val spec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Box(
        modifier =
            modifier
                .onSizeChanged { container = it.toSize() }
                .pointerInput(image) {
                    detectTapGestures(
                        onDoubleTap = { tap ->
                            running?.cancel()
                            val from = transform
                            val to = from.toggledAt(tap, container, fittedSize(container, image.aspectRatio))
                            running =
                                scope.launch {
                                    animate(0f, 1f, animationSpec = spec) { fraction, _ ->
                                        transform = from.lerpTo(to, fraction)
                                    }
                                }
                        },
                    )
                }.pointerInput(image) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        running?.cancel()
                        val fitted = fittedSize(container, image.aspectRatio)
                        transform = transform.zoomedBy(zoom, pan, centroid, container, fitted)
                    }
                }.clipToBounds(),
    ) {
        ViewerImage(
            image = image,
            modifier =
                Modifier.fillMaxSize().graphicsLayer {
                    scaleX = transform.scale
                    scaleY = transform.scale
                    translationX = transform.offset.x
                    translationY = transform.offset.y
                },
        )
    }
}

// The picture the detail already downloaded is drawn first and shows at once; the sharper one fades in
// over it. Both are fitted, never cropped.
@Composable
private fun ViewerImage(
    image: ArtworkImageUi,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = image.url(DETAIL_IMAGE_WIDTH),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(image.url(VIEWER_IMAGE_WIDTH))
                    .crossfade(true)
                    .build(),
            contentDescription = image.contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun ZoomTransform.lerpTo(
    other: ZoomTransform,
    fraction: Float,
): ZoomTransform =
    ZoomTransform(
        scale = scale + (other.scale - scale) * fraction,
        offset = lerp(offset, other.offset, fraction),
    )
