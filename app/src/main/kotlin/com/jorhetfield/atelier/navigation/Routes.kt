package com.jorhetfield.atelier.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

@Serializable
data class DetailRoute(
    val artworkId: Int,
) : NavKey

// Carries what the viewer needs instead of an id, so it opens without asking for the artwork again.
@Serializable
data class ViewerRoute(
    val imageId: String,
    val contentDescription: String?,
    val aspectRatio: Float,
    val sourceWidth: Int?,
) : NavKey
