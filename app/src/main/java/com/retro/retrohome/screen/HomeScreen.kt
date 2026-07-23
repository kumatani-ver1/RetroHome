package com.retro.retrohome.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.retro.retrohome.AppFont
import com.retro.retrohome.component.PageNavButton
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
    modifier: Modifier = Modifier,
    leftButtonText: String = "◀",
    rightButtonText: String = "▶",
    leftIconUri: String? = null,
    rightIconUri: String? = null,
    onLeftButtonLongClick: () -> Unit = {},
    onRightButtonLongClick: () -> Unit = {}
) {
    var openProgress by remember { mutableFloatStateOf(0f) }
    var currentPage by remember { mutableIntStateOf(0) }
    var isForwarding by remember { mutableStateOf(true) }

    val slotsPerPage = 10
    val maxPages = (appSlots.size + slotsPerPage - 1) / slotsPerPage.coerceAtLeast(1)
    val totalPages = if (maxPages < 1) 1 else maxPages

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

        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (isForwarding) {
                    (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } + fadeOut())
                } else {
                    (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeOut())
                }
            },
            label = "pageTransition"
        ) { page ->
            val startIndex = page * slotsPerPage

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = 300.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 160.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(slotsPerPage) { indexOnPage ->
                    val actualSlotIndex = startIndex + indexOnPage
                    val appIcon = appSlots.getOrNull(actualSlotIndex)
                    RetroAppItem(
                        appIcon = appIcon,
                        currentFont = currentFont,
                        onClick = { onSlotClick(actualSlotIndex) },
                        onLongClick = { onSlotLongClick(actualSlotIndex) }
                    )
                }
            }
        }

        // 左右のページ送りボタン
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左ボタン
            PageNavButton(
                text = leftButtonText,
                iconUri = leftIconUri,
                onClick = {
                    isForwarding = false
                    if (currentPage > 0) {
                        currentPage--
                    } else {
                        currentPage = totalPages - 1
                    }
                },
                onLongClick = onLeftButtonLongClick
            )

            // 右ボタン
            PageNavButton(
                text = rightButtonText,
                iconUri = rightIconUri,
                onClick = {
                    isForwarding = true
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        currentPage = 0
                    }
                },
                onLongClick = onRightButtonLongClick
            )
        }

        // アプリドロワー検出エリア
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
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

        // アプリドロワー画面
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