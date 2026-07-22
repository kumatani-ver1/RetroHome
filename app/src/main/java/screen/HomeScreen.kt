package com.retro.retrohome.ui.screen

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
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.ui.component.RetroAppItem
import kotlin.math.roundToInt

/**
 * ホーム画面本体。
 * 画面最下部の取っ手エリア（やや広め）を上にスワイプするとアプリ一覧が下からせり上がる。
 * 一覧が開いている間は、画面全体のどこを下にスワイプしても閉じる。
 */
@Composable
fun HomeScreen(
    appSlots: List<AppIcon?>, // 追加：ホーム画面に並べる10個の枠（空ならnull）
    appIcons: List<AppIcon>,  // 既存：ドロワー（一覧）用の全アプリデータ
    onSlotClick: (Int) -> Unit, // 追加：枠がタップ/長押しされた時に「何番目か」を返す
    onLongClickIcon: (AppIcon) -> Unit, // 既存：ドロワー内のアイコン長押し等
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

        // 背面：ホーム画面のアイコングリッド（2列×5行のインベントリ風レイアウト）
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = 300.dp, // 元の余白を維持
                start = 16.dp,
                end = 16.dp,
                bottom = 120.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 固定で10個のスロットを描画する
            items(10) { index ->
                val appIcon = appSlots.getOrNull(index)
                RetroAppItem(
                    appIcon = appIcon,
                    onClick = { onSlotClick(index) },
                    onLongClick = { onSlotClick(index) } // 長押しでも同じく選択モードへ
                )
            }
        }

        // 画面最下部の取っ手エリア
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

        // 前面：アプリ一覧画面（ドロワー）
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