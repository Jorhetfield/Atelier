package com.jorhetfield.atelier.ui.model

import com.jorhetfield.atelier.domain.model.DominantColor

data class ArtworkUi(
    val id: Int,
    val title: String,
    val artist: String?,
    val image: ArtworkImageUi?,
)

data class ArtworkImageUi(
    val id: String,
    val contentDescription: String?,
    val placeholder: String?,
    val aspectRatio: Float,
    val dominantColor: DominantColor? = null,
    val sourceWidth: Int? = null,
)

data class ArtworkDetailUi(
    val id: Int,
    val title: String,
    val artist: String?,
    val date: String?,
    val medium: String?,
    val origin: String?,
    val dimensions: String?,
    val classification: String?,
    val style: String?,
    val isPublicDomain: Boolean,
    val image: ArtworkImageUi?,
    val dominantColor: DominantColor?,
)
