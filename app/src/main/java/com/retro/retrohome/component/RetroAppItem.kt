package com.retro.retrohome.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // 押し込み判定用の State
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 押している間だけ #2E8BC0 に変える
    val activeColor = Color(0xFF2E8BC0)
    val borderColor = if (isPressed) activeColor else Color.White
    val strokeColor = if (isPressed) activeColor else Color.White

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

    // アイコン画像（カスタムURIまたはデフォルトDrawable）の読み込み＆加工処理
    val displayBitmap = remember(appIcon?.iconUri, appIcon?.icon) {
        if (appIcon == null) return@remember null

        // 1. カスタム画像 URI が設定されている場合、読み込みを試みる
        if (!appIcon.iconUri.isNullOrEmpty()) {
            try {
                val uri = android.net.Uri.parse(appIcon.iconUri)
                val loaded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                if (loaded != null) return@remember loaded
            } catch (e: Exception) {
                // URI読み込み失敗時は下のデフォルトアイコン表示へフォールバック
            }
        }

        // 2. カスタム画像がない（または読み込み失敗）場合、デフォルトアイコンを描画
        val drawable = appIcon.icon ?: return@remember null
        val sizePx = (52 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            // AdaptiveIconDrawable の円形マスクを解体し、四角形として合成描画する
            drawable.background?.let { bg ->
                bg.setBounds(0, 0, sizePx, sizePx)
                bg.draw(canvas)
            }
            drawable.foreground?.let { fg ->
                fg.setBounds(0, 0, sizePx, sizePx)
                fg.draw(canvas)
            }
        } else {
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
        }
        bitmap
    }

    Surface(
        modifier = modifier.height(64.dp),
        color = Color.Black,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, borderColor),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 左側：正方形のアイコン表示エリア
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(52.dp)
                    .clip(RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = appIcon?.label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
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
                        val lineHeight = nativePaint.fontSpacing + 5.dp.toPx()

                        val totalHeight = lineHeight * lines.size
                        var y = (size.height - totalHeight) / 2f + nativePaint.textSize
                        val x = 5.dp.toPx()

                        lines.forEach { line ->
                            // 1. 外枠（STROKE）
                            nativePaint.style = android.graphics.Paint.Style.STROKE
                            nativePaint.strokeWidth = 4.dp.toPx()
                            nativePaint.color = strokeColor.toArgb()
                            canvas.nativeCanvas.drawText(line, x, y, nativePaint)

                            // 2. 黒い太文字本体（FILL）
                            nativePaint.style = android.graphics.Paint.Style.FILL
                            nativePaint.color = Color.Black.toArgb()
                            canvas.nativeCanvas.drawText(line, x, y, nativePaint)

                            y += lineHeight
                        }
                    }
                }
            }
        }
    }
}