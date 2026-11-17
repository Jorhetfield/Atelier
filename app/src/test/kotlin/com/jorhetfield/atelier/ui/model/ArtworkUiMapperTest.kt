package com.jorhetfield.atelier.ui.model

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.ArtworkImage
import com.jorhetfield.atelier.domain.model.DominantColor
import com.jorhetfield.atelier.mocks.artwork
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ArtworkUiMapperTest {
    private fun image(
        width: Int?,
        height: Int?,
    ) = ArtworkImage(id = "abc", altText = "A park", placeholder = "lqip", width = width, height = height)

    @Test
    fun `a card shows the artist name without nationality and dates, the detail keeps them`() {
        val full = artwork().copy(artist = "Georges Seurat (French, 1859-1891)")

        assertEquals("Georges Seurat", full.toUi().artist)
        assertEquals("Georges Seurat (French, 1859-1891)", full.toDetailUi().artist)
    }

    @Test
    fun `the image keeps its proportions`() {
        val ui = artwork(image = image(width = 400, height = 200)).toUi()

        assertEquals(2f, ui.image?.aspectRatio)
    }

    @Test
    fun `an image without dimensions gets a default ratio`() {
        assertEquals(4f / 3f, artwork(image = image(null, null)).toUi().image?.aspectRatio)
        assertEquals(4f / 3f, artwork(image = image(0, 0)).toUi().image?.aspectRatio)
    }

    @Test
    fun `the dominant color travels with the image and with the detail`() {
        val color = DominantColor(hue = 59, saturation = 12, lightness = 52)
        val source = artwork(image = image(10, 10)).copy(dominantColor = color)

        assertEquals(color, source.toUi().image?.dominantColor)
        assertEquals(color, source.toDetailUi().image?.dominantColor)
        assertEquals(color, source.toDetailUi().dominantColor)
    }

    @Test
    fun `a color with enough saturation tints as itself`() {
        val color = DominantColor(hue = 200, saturation = 80, lightness = 50)

        assertEquals(color.toColor(), color.toTint())
    }

    @Test
    fun `a nearly grey color is given some saturation so the tint is visible`() {
        val grey = DominantColor(hue = 59, saturation = 5, lightness = 52)

        assertNotEquals(grey.toColor(), grey.toTint())
    }

    @Test
    fun `an artwork without an image has none in the ui either`() {
        assertNull(artwork(image = null).toUi().image)
    }

    @Test
    fun `the image url asks for the width the screen needs`() {
        val ui = artwork(image = image(10, 10)).toUi()

        assertEquals("https://www.artic.edu/iiif/2/abc/full/843,/0/default.jpg", ui.image?.url(843))
    }

    @Test
    fun `alt text and placeholder carry over`() {
        val ui = artwork(image = image(10, 10)).toUi()

        assertEquals("A park", ui.image?.contentDescription)
        assertEquals("lqip", ui.image?.placeholder)
    }

    @Test
    fun `every exception maps to an error kind`() {
        assertEquals(ErrorKind.Offline, ArtworkException.NetworkUnavailable().toErrorKind())
        assertEquals(ErrorKind.RateLimited, ArtworkException.RateLimited().toErrorKind())
        assertEquals(ErrorKind.NotFound, ArtworkException.NotFound().toErrorKind())
        assertEquals(ErrorKind.Generic, ArtworkException.ServerError(500).toErrorKind())
        assertEquals(ErrorKind.Generic, ArtworkException.Unknown().toErrorKind())
    }
}
