package com.retro.retrohome.component

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * 押すと下に深く沈み込むレトロ風ページ送りボタン（画像表示＆長押し対応）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageNavButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconUri: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 0.dp,
        label = "buttonPressAnimation"
    )

    val density = LocalDensity.current
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .size(width = 64.dp, height = 48.dp)
            .offset { IntOffset(0, with(density) { offsetY.roundToPx() }) }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = Color.Black,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!iconUri.isNullOrEmpty()) {
                // 枠一杯に表示するため padding を除去し ContentScale.Crop に変更
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(iconUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}