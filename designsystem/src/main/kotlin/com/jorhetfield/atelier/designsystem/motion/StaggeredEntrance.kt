package com.jorhetfield.atelier.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Lazy items leave and re-enter composition while scrolling; this remembers which ones already
// played their entrance so they don't replay every time they come back into view.
public class PlayedEntrances {
    private val keys = HashSet<Any>()

    internal fun hasPlayed(key: Any): Boolean = key in keys

    internal fun markPlayed(key: Any) {
        keys += key
    }
}

@Composable
public fun rememberPlayedEntrances(): PlayedEntrances = remember { PlayedEntrances() }

private const val STAGGER_MILLIS = 45L
private const val STAGGER_GROUP = 8

@Composable
public fun Modifier.staggeredEntrance(
    key: Any,
    index: Int,
    played: PlayedEntrances,
): Modifier {
    // Previews and screenshot tests capture a single frame, so the end state is what must be drawn.
    val inspecting = LocalInspectionMode.current
    val alreadyPlayed = remember(key) { inspecting || played.hasPlayed(key) }
    val progress = remember(key) { Animatable(if (alreadyPlayed) 1f else 0f) }
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(key) {
        if (alreadyPlayed) return@LaunchedEffect
        delay((index % STAGGER_GROUP) * STAGGER_MILLIS)
        played.markPlayed(key)
        progress.animateTo(1f, spec)
    }

    return graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * 40.dp.toPx()
    }
}
