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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.retro.retrohome.AppFont
import com.retro.retrohome.model.AppIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroAppItem(
    appIcon: AppIcon?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentFont: AppFont? = null
) {
    val context = LocalContext.current
    val fontFamilyResolver = LocalFontFamilyResolver.current

    // 太字（FontWeight.Bold）で Typeface を解決
    val resolvedTypeface = remember(currentFont, fontFamilyResolver) {
        val family = currentFont?.fontFamily ?: FontFamily.Default
        val result = fontFamilyResolver.resolve(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        )
        result.value as? android.graphics.Typeface
    }

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
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        appIcon.icon != null -> {
                            Image(
                                painter = rememberDrawablePainter(drawable = appIcon.icon),
                                contentDescription = appIcon.label,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // 右側：ラベル表示エリア（太字＋くっきり太い白枠の袋文字）
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                val labelText = appIcon?.label ?: "EMPTY"

                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val fontSizePx = 16.sp.toPx()

                    drawIntoCanvas { canvas ->
                        val nativePaint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            textSize = fontSizePx
                            typeface = resolvedTypeface
                            isFakeBoldText = true
                        }

                        val lines = labelText.split("\n").take(2)
                        val lineHeight = nativePaint.fontSpacing

                        val totalHeight = lineHeight * lines.size
                        var y = (size.height - totalHeight) / 2f + nativePaint.textSize

                        lines.forEach { line ->
                            // 1. 白い外枠（STROKE）
                            nativePaint.style = android.graphics.Paint.Style.STROKE
                            nativePaint.strokeWidth = 4.dp.toPx() // ★白枠の太さを 3.dp -> 5.dp に強化（もっと太くしたい場合は 6.dp に）
                            nativePaint.color = Color.White.toArgb()
                            canvas.nativeCanvas.drawText(line, 0f, y, nativePaint)

                            // 2. 黒い太文字本体（FILL）
                            nativePaint.style = android.graphics.Paint.Style.FILL
                            nativePaint.color = Color.Black.toArgb()
                            canvas.nativeCanvas.drawText(line, 0f, y, nativePaint)

                            y += lineHeight
                        }
                    }
                }
            }
        }
    }
}