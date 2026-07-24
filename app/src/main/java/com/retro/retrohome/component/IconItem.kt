package com.retro.retrohome.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
fun IconItem(
    appIcon: AppIcon,
    onClick: (AppIcon) -> Unit,
    onLongClick: (AppIcon) -> Unit,
    modifier: Modifier = Modifier,
    currentFont: AppFont? = null
) {
    val context = LocalContext.current

    val iconBitmap = remember(appIcon.icon) {
        val drawable = appIcon.icon ?: return@remember null
        val sizePx = (40 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
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

    val fontFamilyResolver = LocalFontFamilyResolver.current
    val resolvedTypeface = remember(currentFont, fontFamilyResolver) {
        val family = currentFont?.fontFamily ?: FontFamily.Default
        val result = fontFamilyResolver.resolve(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        )
        result.value as? android.graphics.Typeface
    }

    val density = context.resources.displayMetrics.density
    // ★ 文字の折り返し判定幅を広げ、横幅いっぱいに使えるように調整（60 -> 72）
    val estimatedWidthPx = 72 * density

    val rawLines = remember(appIcon.label, resolvedTypeface, density) {
        val nativePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 12.sp.value * density
            typeface = resolvedTypeface
            isFakeBoldText = true
        }

        val linesList = mutableListOf<String>()
        val raw = appIcon.label.split("\n")

        for (rawLine in raw) {
            var currentLine = ""
            var i = 0
            while (i < rawLine.length) {
                val c = rawLine[i]
                val testLine = currentLine + c
                if (nativePaint.measureText(testLine) > estimatedWidthPx && currentLine.isNotEmpty()) {
                    val lastSpaceIndex = currentLine.lastIndexOfAny(charArrayOf(' ', ' '))
                    if (lastSpaceIndex != -1 && lastSpaceIndex > 0) {
                        val lineToKeep = currentLine.substring(0, lastSpaceIndex).trimEnd()
                        linesList.add(lineToKeep)
                        currentLine = currentLine.substring(lastSpaceIndex + 1) + c
                    } else {
                        linesList.add(currentLine)
                        currentLine = c.toString()
                    }
                } else {
                    currentLine = testLine
                }
                i++
            }
            if (currentLine.isNotEmpty()) {
                linesList.add(currentLine)
            }
        }
        linesList
    }

    val targetSp = if (rawLines.size >= 3) 10.sp else 12.sp
    val fontSizePx = targetSp.value * density

    val (lines, labelHeight) = remember(appIcon.label, resolvedTypeface, density, targetSp) {
        val nativePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = fontSizePx
            typeface = resolvedTypeface
            isFakeBoldText = true
        }

        val resultLines = mutableListOf<String>()
        val raw = appIcon.label.split("\n")

        for (rawLine in raw) {
            var currentLine = ""
            var i = 0
            while (i < rawLine.length) {
                val c = rawLine[i]
                val testLine = currentLine + c
                if (nativePaint.measureText(testLine) > estimatedWidthPx && currentLine.isNotEmpty()) {
                    val lastSpaceIndex = currentLine.lastIndexOfAny(charArrayOf(' ', ' '))
                    if (lastSpaceIndex != -1 && lastSpaceIndex > 0) {
                        val lineToKeep = currentLine.substring(0, lastSpaceIndex).trimEnd()
                        resultLines.add(lineToKeep)
                        currentLine = currentLine.substring(lastSpaceIndex + 1) + c
                    } else {
                        resultLines.add(currentLine)
                        currentLine = c.toString()
                    }
                } else {
                    currentLine = testLine
                }
                i++
            }
            if (currentLine.isNotEmpty()) {
                resultLines.add(currentLine)
            }
        }

        val fontMetrics = nativePaint.fontMetrics
        val singleLineHeight = fontMetrics.descent - fontMetrics.ascent
        val lineSpacing = 1.5f * density
        val strokePaddingPx = 4 * density

        val totalHeightPx = (singleLineHeight * resultLines.size) + (lineSpacing * (resultLines.size - 1)) + strokePaddingPx
        val heightDp = (totalHeightPx / density).dp

        Pair(resultLines, heightDp)
    }

    val lineCount = lines.size

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = { onClick(appIcon) },
                onLongClick = { onLongClick(appIcon) }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(if (lineCount >= 3) 2.dp else if (lineCount == 2) 3.dp else 4.dp))

        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = appIcon.label,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(
            modifier = Modifier.height(
                when {
                    lineCount >= 3 -> 2.dp
                    lineCount == 2 -> 6.dp
                    else -> 10.dp
                }
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelHeight),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    val nativePaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textSize = fontSizePx
                        typeface = resolvedTypeface
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    val fontMetrics = nativePaint.fontMetrics
                    val singleLineHeight = fontMetrics.descent - fontMetrics.ascent
                    val lineSpacing = 1.5f.dp.toPx()
                    val totalHeight = (singleLineHeight * lines.size) + (lineSpacing * (lines.size - 1))

                    val startY = (size.height - totalHeight) / 2f - fontMetrics.ascent
                    val x = size.width / 2f

                    lines.forEachIndexed { index, line ->
                        val y = startY + index * (singleLineHeight + lineSpacing)

                        nativePaint.style = android.graphics.Paint.Style.STROKE
                        nativePaint.strokeWidth = 2.5.dp.toPx()
                        nativePaint.color = Color.White.toArgb()
                        canvas.nativeCanvas.drawText(line, x, y, nativePaint)

                        nativePaint.style = android.graphics.Paint.Style.FILL
                        nativePaint.color = Color.Black.toArgb()
                        canvas.nativeCanvas.drawText(line, x, y, nativePaint)
                    }
                }
            }
        }
    }
}