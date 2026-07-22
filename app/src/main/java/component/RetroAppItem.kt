package com.retro.retrohome.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.retro.retrohome.model.AppIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroAppItem(
    appIcon: AppIcon?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(64.dp),
        color = Color.Black,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, Color.White),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 左側：正方形のアイコン表示エリア
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(52.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    appIcon.icon?.let { drawable ->
                        Image(
                            painter = rememberDrawablePainter(drawable = drawable),
                            contentDescription = appIcon.label,
                            modifier = Modifier.size(40.dp) // アイコンのサイズ
                        )
                    }
                }
            }

            // 右側：ラベル表示エリア（最大2行）
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = appIcon?.label ?: "EMPTY",
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}