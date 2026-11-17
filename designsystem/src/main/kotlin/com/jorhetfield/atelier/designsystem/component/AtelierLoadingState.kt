package com.jorhetfield.atelier.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

// LoadingIndicator is still experimental in Material 3 1.5; opted in on purpose, it is the expressive loader.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AtelierLoadingState(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}
