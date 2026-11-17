package com.jorhetfield.atelier.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.LightSystemBarIcons
import com.jorhetfield.atelier.ui.model.ArtworkImageUi

@Composable
fun ArtworkViewerScreen(
    image: ArtworkImageUi,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The background is always black, so both system bars need light icons whatever the theme is.
    LightSystemBarIcons(statusBars = true, navigationBars = true)

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        ZoomableArtwork(image = image, modifier = Modifier.fillMaxSize())

        FilledTonalIconButton(
            onClick = onClose,
            colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.16f),
                    contentColor = Color.White,
                ),
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start))
                    .padding(AtelierTheme.spacing.md),
        ) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.viewer_close))
        }
    }
}
