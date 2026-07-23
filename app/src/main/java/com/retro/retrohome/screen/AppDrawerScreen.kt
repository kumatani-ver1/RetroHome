package com.retro.retrohome.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.component.IconGrid

/**
 * アプリ一覧画面（画面いっぱいに全アプリを表示する）
 *
 * NestedScrollConnection を使用し、「リストのスクロール」と「画面を閉じるドラッグ」を自動調整します。
 * ・リストが下にある時 → 指を下へ動かすと「リストが上にスクロール」する
 * ・リストが一番上の時 → 指を下へ動かすと「画面をドラッグ（閉じる）」する
 */
@Composable
fun AppDrawerScreen(
    appIcons: List<AppIcon>,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    // 画面がどれくらい下に引っ張られているかを記憶する変数
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // スクロールとドラッグの競合を解決する仕組み
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // スクロールが行われる「前」の処理
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero

                // 画面を下に引っ張っている途中で、指を「上」に戻した場合
                // リストをスクロールする前に、引っ張った画面を元に戻す処理を優先する
                if (dragOffset > 0f && available.y < 0f) {
                    val consumeY = if (dragOffset + available.y < 0f) {
                        -dragOffset // 0より上には行かないように調整
                    } else {
                        available.y
                    }
                    dragOffset += consumeY
                    onDrag(consumeY)
                    return Offset(0f, consumeY) // 消費した分をリスト側に伝えない
                }
                return Offset.Zero
            }

            // スクロールが行われた「後」の処理
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // リストが一番上にあり、それ以上スクロールできないのに「下」へ引っ張った場合
                if (source == NestedScrollSource.Drag && available.y > 0f) {
                    dragOffset += available.y
                    onDrag(available.y)
                    return Offset(0f, available.y) // ドラッグとして消費
                }
                return Offset.Zero
            }

            // 指を離した瞬間の処理
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffset > 0f) {
                    onDragEnd()
                    dragOffset = 0f // 状態をリセット
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // この1行で IconGrid のスクロール状態を監視して、自動でドラッグを制御します
            .nestedScroll(nestedScrollConnection)
    ) {
        IconGrid(appIcons = appIcons, onLongClickIcon = onLongClickIcon)
    }
}