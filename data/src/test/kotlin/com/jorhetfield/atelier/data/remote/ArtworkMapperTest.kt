package com.jorhetfield.atelier.data.remote

import com.jorhetfield.atelier.domain.model.DominantColor
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ArtworkMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun artwork(raw: String) = json.decodeFromString<ArtworkDto>(raw).toDomain()

    @Test
    fun `maps every field of a complete artwork`() {
        val artwork =
            artwork(
                """
                {"id":27992,"title":"A Sunday on La Grande Jatte","artist_display":"Georges Seurat\nFrench, 1859-1891",
                 "date_display":"1884-86","medium_display":"Oil on canvas","place_of_origin":"France",
                 "dimensions":"207.5 x 308.1 cm","image_id":"abc","is_public_domain":true,
                 "style_title":"Pointillism","classification_title":"oil on canvas",
                 "color":{"h":59,"l":52,"s":12,"percentage":0.002},
                 "thumbnail":{"lqip":"data:image/gif;base64,AAA","width":9310,"height":6237,"alt_text":"A park"}}
                """.trimIndent(),
            )

        assertEquals("A Sunday on La Grande Jatte", artwork.title)
        assertEquals("Georges Seurat", artwork.artist)
        assertEquals("1884-86", artwork.date)
        assertEquals("Pointillism", artwork.style)
        assertEquals(DominantColor(hue = 59, saturation = 12, lightness = 52), artwork.dominantColor)
        assertEquals("abc", artwork.image?.id)
        assertEquals("A park", artwork.image?.altText)
        assertEquals(9310, artwork.image?.width)
    }

    @Test
    fun `missing and blank values become null instead of empty strings`() {
        val artwork = artwork("""{"id":1,"title":"  ","artist_display":"","medium_display":null}""")

        assertEquals("Untitled", artwork.title)
        assertNull(artwork.artist)
        assertNull(artwork.medium)
        assertNull(artwork.image)
        assertNull(artwork.dominantColor)
        assertEquals(false, artwork.isPublicDomain)
    }

    @Test
    fun `an incomplete color is dropped`() {
        assertNull(artwork("""{"id":1,"color":{"h":59}}""").dominantColor)
    }

    @Test
    fun `an artwork without an image id has no image even if it has a thumbnail`() {
        assertNull(artwork("""{"id":1,"thumbnail":{"lqip":"x"}}""").image)
    }

    @Test
    fun `results with negligible relevance are discarded`() {
        val response =
            json.decodeFromString<SearchResponseDto>(
                """{"data":[{"id":1,"_score":120.4},{"id":2,"_score":0.0000231},{"id":3,"_score":8.1}]}""",
            )

        assertEquals(listOf(1, 3), response.toArtworks().map { it.id })
    }

    @Test
    fun `a result without a score is kept`() {
        val response = json.decodeFromString<SearchResponseDto>("""{"data":[{"id":1}]}""")

        assertEquals(listOf(1), response.toArtworks().map { it.id })
    }
}
