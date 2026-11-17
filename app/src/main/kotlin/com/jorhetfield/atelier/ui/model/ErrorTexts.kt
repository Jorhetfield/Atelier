package com.jorhetfield.atelier.ui.model

import androidx.annotation.StringRes
import com.jorhetfield.atelier.R

@get:StringRes
val ErrorKind.titleRes: Int
    get() =
        when (this) {
            ErrorKind.Offline -> R.string.error_offline_title
            ErrorKind.RateLimited -> R.string.error_rate_limited_title
            ErrorKind.NotFound -> R.string.error_not_found_title
            ErrorKind.Generic -> R.string.error_generic_title
        }

@get:StringRes
val ErrorKind.messageRes: Int
    get() =
        when (this) {
            ErrorKind.Offline -> R.string.error_offline_message
            ErrorKind.RateLimited -> R.string.error_rate_limited_message
            ErrorKind.NotFound -> R.string.error_not_found_message
            ErrorKind.Generic -> R.string.error_generic_message
        }
