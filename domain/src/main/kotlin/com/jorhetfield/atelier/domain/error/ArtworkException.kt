package com.jorhetfield.atelier.domain.error

// Cancellation is not in here on purpose: it has to propagate, never be shown as an error.
public sealed class ArtworkException(
    message: String?,
    cause: Throwable? = null,
) : Exception(message, cause) {
    public class NetworkUnavailable(
        cause: Throwable? = null,
    ) : ArtworkException("Network unavailable", cause)

    public class RateLimited : ArtworkException("Too many requests")

    public class NotFound : ArtworkException("Artwork not found")

    public class ServerError(
        public val code: Int?,
        detail: String? = null,
    ) : ArtworkException(detail ?: "Server error${code?.let { " ($it)" }.orEmpty()}")

    public class Unknown(
        cause: Throwable? = null,
    ) : ArtworkException("Unexpected error", cause)
}
