package com.jorhetfield.atelier.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// A two-way switch between the list and the grid. Both options are always on screen with an icon and a
// word, so the choice is obvious and one tap away, rather than hidden behind an icon in a corner.
@Composable
public fun AtelierLayoutToggle(
    gridSelected: Boolean,
    onGridSelectedChange: (Boolean) -> Unit,
    listLabel: String,
    gridLabel: String,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = !gridSelected,
            onClick = { onGridSelectedChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { Icon(ListIcon, contentDescription = null, modifier = Modifier.size(IconSize)) },
        ) {
            Text(listLabel)
        }
        SegmentedButton(
            selected = gridSelected,
            onClick = { onGridSelectedChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { Icon(GridIcon, contentDescription = null, modifier = Modifier.size(IconSize)) },
        ) {
            Text(gridLabel)
        }
    }
}

private val IconSize = 18.dp

// Drawn here because the two icons are not in material-icons-core and the extended set is a lot of
// weight for two shapes.
private fun PathBuilder.square(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
    moveTo(x, y)
    horizontalLineToRelative(width)
    verticalLineToRelative(height)
    horizontalLineToRelative(-width)
    close()
}

private val ListIcon: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "AtelierList",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            for (top in listOf(4f, 10f, 16f)) {
                square(3f, top, 4f, 4f)
                square(9f, top, 12f, 4f)
            }
        }.build()
}

private val GridIcon: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "AtelierGrid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            square(3f, 3f, 8f, 8f)
            square(13f, 3f, 8f, 8f)
            square(3f, 13f, 8f, 8f)
            square(13f, 13f, 8f, 8f)
        }.build()
}
