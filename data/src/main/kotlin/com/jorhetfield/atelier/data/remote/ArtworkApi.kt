package com.jorhetfield.atelier.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PAGE_SIZE = 20

private const val FIELDS =
    "id,title,artist_display,date_display,medium_display,place_of_origin,dimensions," +
        "image_id,color,thumbnail,is_public_domain,style_title,classification_title"

internal interface ArtworkApi {
    @GET("artworks/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("fields") fields: String = FIELDS,
    ): SearchResponseDto

    @GET("artworks/{id}")
    suspend fun artwork(
        @Path("id") id: Int,
        @Query("fields") fields: String = FIELDS,
    ): ArtworkResponseDto
}
