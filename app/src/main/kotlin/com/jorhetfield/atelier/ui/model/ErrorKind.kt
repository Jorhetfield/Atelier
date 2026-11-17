package com.jorhetfield.atelier.ui.model

import com.jorhetfield.atelier.domain.error.ArtworkException

enum class ErrorKind {
    Offline,
    RateLimited,
    NotFound,
    Generic,
}

fun ArtworkException.toErrorKind(): ErrorKind =
    when (this) {
        is ArtworkException.NetworkUnavailable -> ErrorKind.Offline
        is ArtworkException.RateLimited -> ErrorKind.RateLimited
        is ArtworkException.NotFound -> ErrorKind.NotFound
        is ArtworkException.ServerError, is ArtworkException.Unknown -> ErrorKind.Generic
    }
