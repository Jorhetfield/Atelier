package com.jorhetfield.atelier.screenshottests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme

// Dynamic color depends on the device wallpaper, so references use the fixed palette.
@Composable
internal fun ScreenshotSurface(content: @Composable () -> Unit) {
    AtelierTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(AtelierTheme.spacing.lg)) { content() }
        }
    }
}

@Composable
internal fun SampleImage() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.primaryContainer),
                    ),
                ),
    )
}
