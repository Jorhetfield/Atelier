package com.jorhetfield.atelier.data.remote

import com.jorhetfield.atelier.domain.error.ArtworkException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private val errorJson = Json { ignoreUnknownKeys = true }

internal suspend fun <T> translatingErrors(call: suspend () -> T): T =
    try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ArtworkException) {
        throw e
    } catch (e: HttpException) {
        throw e.toArtworkException()
    } catch (e: IOException) {
        throw ArtworkException.NetworkUnavailable(e)
    } catch (e: SerializationException) {
        throw ArtworkException.ServerError(code = null, detail = "Unreadable response")
    } catch (e: Exception) {
        throw ArtworkException.Unknown(e)
    }

private fun HttpException.toArtworkException(): ArtworkException =
    when (code()) {
        404 -> ArtworkException.NotFound()
        429 -> ArtworkException.RateLimited()
        else -> ArtworkException.ServerError(code(), errorDetail())
    }

private fun HttpException.errorDetail(): String? =
    try {
        response()?.errorBody()?.string()?.let { errorJson.decodeFromString<ErrorBodyDto>(it).detail }
    } catch (e: SerializationException) {
        null
    } catch (e: IOException) {
        null
    }
