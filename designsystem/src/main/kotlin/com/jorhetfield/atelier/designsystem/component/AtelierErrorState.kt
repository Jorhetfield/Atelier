package com.jorhetfield.atelier.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public fun AtelierErrorState(
    title: String,
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateLayout(
        icon = Icons.Rounded.Warning,
        iconBackground = MaterialTheme.colorScheme.errorContainer,
        iconTint = MaterialTheme.colorScheme.onErrorContainer,
        title = title,
        message = message,
        modifier = modifier,
        action = {
            Button(onClick = onRetry) { Text(retryLabel) }
        },
    )
}
