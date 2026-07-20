package com.retro.retrohome.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.ui.component.IconGrid
import kotlin.math.abs

/**
 * アプリ一覧画面（画面いっぱいに全アプリを表示する）
 *
 * IconGrid（スクロール・タップ可能）の上に透明な操作レイヤーを重ねているが、
 * 単純に全タッチを奪うのではなく「一定距離動いたら初めてドラッグとみなす」判定を行う。
 * これにより、指を動かさない通常のタップは下のIconGrid（将来のアイコン起動処理）に届き、
 * 一定距離以上の縦方向の動きだけを「閉じる」操作として横取りする。
 */
@Composable
fun AppDrawerScreen(
    appIcons: List<AppIcon>,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val touchSlop = LocalViewConfiguration.current.touchSlop

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconGrid(appIcons = appIcons)

        // 透明な操作レイヤー：一定距離動くまでは何もせず、下のタップをそのまま通す
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(touchSlop) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        var totalDragY = 0f
                        var isDragging = false

                        do {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val deltaY = change.position.y - change.previousPosition.y
                            totalDragY += deltaY

                            if (!isDragging && abs(totalDragY) > touchSlop) {
                                isDragging = true
                            }

                            if (isDragging) {
                                change.consume() // ここから先はドラッグとして横取りする
                                onDrag(deltaY)
                            }
                            // isDragging が false の間は consume しない → タップは下に届く
                        } while (change.pressed)

                        if (isDragging) {
                            onDragEnd()
                        }
                    }
                }
        )
    }
}