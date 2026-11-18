package com.jorhetfield.atelier.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PAGE_SIZE = 20
internal const val HIGHLIGHTS_SIZE = 12

// The API refuses (403) anything past its first 1000 results, which is page 50 at this page size.
internal const val MAX_PAGE = 50

private const val FIELDS =
    "id,title,artist_display,date_display,medium_display,place_of_origin,dimensions," +
        "image_id,color,thumbnail,is_public_domain,style_title,classification_title"

internal interface ArtworkApi {
    @GET("artworks/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("fields") fields: String = FIELDS,
    ): SearchResponseDto

    // The museum flags its showpieces and ranks them; rank 1 is the most important.
    @GET("artworks/search")
    suspend fun highlights(
        @Query("query[term][is_boosted]") boosted: Boolean = true,
        @Query("sort[boost_rank]") sort: String = "asc",
        @Query("page") page: Int,
        @Query("limit") limit: Int = HIGHLIGHTS_SIZE,
        @Query("fields") fields: String = FIELDS,
    ): SearchResponseDto

    @GET("artworks/{id}")
    suspend fun artwork(
        @Path("id") id: Int,
        @Query("fields") fields: String = FIELDS,
    ): ArtworkResponseDto
}
