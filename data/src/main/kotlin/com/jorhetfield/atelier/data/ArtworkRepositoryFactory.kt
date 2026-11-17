package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.cache.TtlCache
import com.jorhetfield.atelier.data.remote.ArtworkApi
import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

private val API_URL = "https://api.artic.edu/api/v1/".toHttpUrl()
private val CACHE_TTL = 3.minutes

public fun createArtworkRepository(): ArtworkRepository = buildArtworkRepository()

internal fun buildArtworkRepository(
    baseUrl: HttpUrl = API_URL,
    timeSource: TimeSource = TimeSource.Monotonic,
    cacheTtl: Duration = CACHE_TTL,
): ArtworkRepository {
    val client =
        OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("AIC-User-Agent", "Atelier")
                        .build(),
                )
            }.build()

    val json = Json { ignoreUnknownKeys = true }
    val api =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ArtworkApi::class.java)

    return ArtworkRepositoryImpl(
        api = api,
        searchCache = TtlCache<String, List<Artwork>>(cacheTtl, timeSource),
        artworkCache = TtlCache<Int, Artwork>(cacheTtl, timeSource),
    )
}
