package com.jorhetfield.atelier.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

@Serializable
data class DetailRoute(
    val artworkId: Int,
) : NavKey
