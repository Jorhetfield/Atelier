package com.jorhetfield.atelier.ui.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ZoomMathTest {
    private val container = Size(1000f, 1000f)
    private val square = Size(1000f, 1000f)

    @Test
    fun `a wide picture is fitted to the width of the container`() {
        assertEquals(Size(1000f, 500f), fittedSize(Size(1000f, 2000f), aspectRatio = 2f))
    }

    @Test
    fun `a tall picture is fitted to the height of the container`() {
        assertEquals(Size(500f, 1000f), fittedSize(Size(2000f, 1000f), aspectRatio = 0.5f))
    }

    @Test
    fun `an empty container or a broken ratio gives an empty size`() {
        assertEquals(Size.Zero, fittedSize(Size.Zero, aspectRatio = 1.5f))
        assertEquals(Size.Zero, fittedSize(container, aspectRatio = 0f))
    }

    @Test
    fun `zooming never goes past the limits`() {
        val zoomedIn = ZoomTransform().zoomedBy(100f, Offset.Zero, container.center, container, square)
        val zoomedOut = zoomedIn.zoomedBy(0.001f, Offset.Zero, container.center, container, square)

        assertEquals(MAX_ZOOM, zoomedIn.scale)
        assertEquals(MIN_ZOOM, zoomedOut.scale)
    }

    @Test
    fun `the point under the fingers stays under the fingers while zooming`() {
        val centroid = Offset(750f, 500f)

        val zoomed = ZoomTransform().zoomedBy(2f, Offset.Zero, centroid, container, square)

        // Where the picture point that was under the centroid ends up on screen.
        val before = centroid - container.center
        val after = container.center + before * zoomed.scale + zoomed.offset
        assertEquals(centroid, after)
    }

    @Test
    fun `dragging stops at the edges of the picture`() {
        val zoomed = ZoomTransform(scale = 2f)

        val dragged = zoomed.zoomedBy(1f, Offset(5000f, -5000f), container.center, container, square)

        assertEquals(Offset(500f, -500f), dragged.offset)
    }

    @Test
    fun `an axis where the picture fits stays centered`() {
        val wide = Size(1000f, 400f)

        val zoomed = ZoomTransform().zoomedBy(2f, Offset(300f, 300f), container.center, container, wide)

        // At 2x the picture is 800 tall, still less than the 1000 container: no vertical drag.
        assertEquals(0f, zoomed.offset.y)
        assertEquals(300f, zoomed.offset.x)
    }

    @Test
    fun `zooming back out recenters the picture`() {
        val dragged = ZoomTransform(scale = 2f).zoomedBy(1f, Offset(400f, 400f), container.center, container, square)

        val reset = dragged.zoomedBy(0.5f, Offset.Zero, container.center, container, square)

        assertEquals(MIN_ZOOM, reset.scale)
        assertEquals(Offset.Zero, reset.offset)
    }

    @Test
    fun `a double tap zooms in on the spot and a second one goes back`() {
        val tap = Offset(800f, 300f)

        val zoomedIn = ZoomTransform().toggledAt(tap, container, square)
        val zoomedOut = zoomedIn.toggledAt(tap, container, square)

        assertEquals(DOUBLE_TAP_ZOOM, zoomedIn.scale)
        assertEquals(ZoomTransform(), zoomedOut)
    }
}
