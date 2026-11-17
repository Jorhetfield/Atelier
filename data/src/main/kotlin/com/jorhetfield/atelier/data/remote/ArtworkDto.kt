package com.jorhetfield.atelier.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResponseDto(
    val data: List<ArtworkDto> = emptyList(),
)

@Serializable
internal data class ArtworkResponseDto(
    val data: ArtworkDto,
)

@Serializable
internal data class ArtworkDto(
    val id: Int,
    val title: String? = null,
    @SerialName("_score") val score: Double? = null,
    @SerialName("artist_display") val artistDisplay: String? = null,
    @SerialName("date_display") val dateDisplay: String? = null,
    @SerialName("medium_display") val mediumDisplay: String? = null,
    @SerialName("place_of_origin") val placeOfOrigin: String? = null,
    val dimensions: String? = null,
    @SerialName("image_id") val imageId: String? = null,
    val color: ColorDto? = null,
    val thumbnail: ThumbnailDto? = null,
    @SerialName("is_public_domain") val isPublicDomain: Boolean? = null,
    @SerialName("style_title") val styleTitle: String? = null,
    @SerialName("classification_title") val classificationTitle: String? = null,
)

@Serializable
internal data class ColorDto(
    val h: Int? = null,
    val s: Int? = null,
    val l: Int? = null,
)

@Serializable
internal data class ThumbnailDto(
    val lqip: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("alt_text") val altText: String? = null,
)

@Serializable
internal data class ErrorBodyDto(
    val detail: String? = null,
)
