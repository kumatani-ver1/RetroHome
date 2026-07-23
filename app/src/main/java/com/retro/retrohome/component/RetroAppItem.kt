package com.retro.retrohome.component

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

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
                    when {
                        // 1. カスタムアイコンのUriがある場合
                        !appIcon.iconUri.isNullOrEmpty() -> {
                            val bitmap = remember(appIcon.iconUri) {
                                try {
                                    val uri = android.net.Uri.parse(appIcon.iconUri)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                            decoder.isMutableRequired = true
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = appIcon.label,
                                    modifier = Modifier.fillMaxSize(), // ★枠いっぱいに広げる
                                    contentScale = ContentScale.Crop   // ★比率を維持して枠を埋める
                                )
                            }
                        }
                        // 2. 通常の標準アイコンの場合
                        appIcon.icon != null -> {
                            Image(
                                painter = rememberDrawablePainter(drawable = appIcon.icon),
                                contentDescription = appIcon.label,
                                modifier = Modifier.fillMaxSize(), // ★枠いっぱいに広げる
                                contentScale = ContentScale.Crop   // ★比率を維持して枠を埋める
                            )
                        }
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