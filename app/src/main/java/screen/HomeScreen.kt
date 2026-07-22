package com.retro.retrohome.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.retro.retrohome.ui.component.IconGrid
import kotlin.math.roundToInt

/**
 * ホーム画面本体。
 * 画面最下部の取っ手エリア（やや広め）を上にスワイプするとアプリ一覧が下からせり上がる。
 * 一覧が開いている間は、画面全体のどこを下にスワイプしても閉じる。
 *
 * @param onLongClickIcon アイコンが長押しされた時に呼ばれる（ラベル編集ダイアログを開くため）
 */
@Composable
fun HomeScreen(
    appIcons: List<AppIcon>,
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
        // 感度調整：画面の1/3程度の距離で全開になる
        val dragSensitivityPx = fullHeightPx / 3f

        // 背面：ホーム画面のアイコングリッド（上部にウィジェットエリア分の余白を確保）
        // STEP3でアプリ選択機能を実装するまでの暫定処置として、一覧の先頭20個のみ表示する
        IconGrid(
            appIcons = appIcons.take(20),
            topPadding = 300.dp,
            onLongClickIcon = onLongClickIcon
        )

        // 画面最下部の取っ手エリア（開く操作専用・以前の倍の高さ）
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(96.dp) // 48dp の倍
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

        // 前面：アプリ一覧画面。下からせり上がる/沈み込むように表示。
        // 開いている間は画面全体で「下にスワイプして閉じる」操作を受け付ける
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