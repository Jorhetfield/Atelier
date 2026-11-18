package com.jorhetfield.atelier.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme

// The optional action sits at the end of the title row ("See all"). Its description names the row it
// belongs to, so a screen reader hears "See all Monet" rather than the same words once per shelf.
@Composable
public fun AtelierSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionContentDescription: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = AtelierTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                // The button carries its own horizontal padding, so less is taken here to keep its text
                // lined up with the content margin.
                .padding(start = spacing.lg, end = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier =
                    Modifier.semantics {
                        actionContentDescription?.let { contentDescription = it }
                    },
            ) {
                Text(actionLabel)
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = spacing.xs).size(18.dp),
                )
            }
        }
    }
}
