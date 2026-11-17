package com.jorhetfield.atelier.ui.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import kotlin.math.max

const val MIN_ZOOM = 1f
const val MAX_ZOOM = 5f
const val DOUBLE_TAP_ZOOM = 2.5f

// The picture is drawn centered, then scaled around its center and shifted by `offset` (in pixels).
data class ZoomTransform(
    val scale: Float = MIN_ZOOM,
    val offset: Offset = Offset.Zero,
)

fun fittedSize(
    container: Size,
    aspectRatio: Float,
): Size {
    if (container.width <= 0f || container.height <= 0f || aspectRatio <= 0f) return Size.Zero
    return if (container.width / container.height > aspectRatio) {
        Size(width = container.height * aspectRatio, height = container.height)
    } else {
        Size(width = container.width, height = container.width / aspectRatio)
    }
}

fun ZoomTransform.zoomedBy(
    zoom: Float,
    pan: Offset,
    centroid: Offset,
    container: Size,
    fitted: Size,
): ZoomTransform {
    val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
    val applied = newScale / scale
    val fromCenter = centroid - container.center
    val anchored = fromCenter - (fromCenter - offset) * applied
    return ZoomTransform(newScale, clampOffset(anchored + pan, newScale, container, fitted))
}

fun ZoomTransform.toggledAt(
    tap: Offset,
    container: Size,
    fitted: Size,
): ZoomTransform =
    if (scale > MIN_ZOOM + EPSILON) {
        ZoomTransform()
    } else {
        zoomedBy(DOUBLE_TAP_ZOOM, Offset.Zero, tap, container, fitted)
    }

private fun clampOffset(
    offset: Offset,
    scale: Float,
    container: Size,
    fitted: Size,
): Offset {
    val maxX = max(0f, (fitted.width * scale - container.width) / 2f)
    val maxY = max(0f, (fitted.height * scale - container.height) / 2f)
    return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
}

private const val EPSILON = 0.01f
