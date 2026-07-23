package com.retro.retrohome.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.retro.retrohome.AppFont
import com.retro.retrohome.component.RetroAppItem
import com.retro.retrohome.model.AppIcon
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    appSlots: List<AppIcon?>,
    appIcons: List<AppIcon>,
    currentFont: AppFont,
    onSlotClick: (Int) -> Unit,
    onSlotLongClick: (Int) -> Unit,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    var openProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = openProgress,
        animationSpec = tween(durationMillis = 200),
        label = "drawerOpenProgress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val fullHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val dragSensitivityPx = fullHeightPx / 3f

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = 300.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 120.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(10) { index ->
                val appIcon = appSlots.getOrNull(index)
                RetroAppItem(
                    appIcon = appIcon,
                    currentFont = currentFont,
                    onClick = { onSlotClick(index) },
                    onLongClick = { onSlotLongClick(index) }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(96.dp)
                .pointerInput(dragSensitivityPx) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            openProgress = (openProgress - dragAmount / dragSensitivityPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            openProgress = if (openProgress > 0.5f) 1f else 0f
                        }
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    val yOffset = ((1f - animatedProgress) * fullHeightPx).roundToInt()
                    IntOffset(0, yOffset)
                }
        ) {
            AppDrawerScreen(
                appIcons = appIcons,
                onDrag = { dragAmount ->
                    openProgress = (openProgress - dragAmount / dragSensitivityPx).coerceIn(0f, 1f)
                },
                onDragEnd = {
                    openProgress = if (openProgress > 0.5f) 1f else 0f
                },
                onLongClickIcon = onLongClickIcon
            )
        }
    }
}