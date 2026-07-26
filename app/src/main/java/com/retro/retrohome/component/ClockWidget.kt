package com.retro.retrohome.component

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.retrohome.AppFont
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun currentTimeText(): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date())
}

/**
 * ドックに配置する時計ウィジェット（他のドックボタンと同じ64x64）。
 * ページ番号と同じ「袋文字」スタイルで表示し、タップで端末の時計アプリを開く。
 */
@Composable
fun ClockWidget(
    currentFont: AppFont? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var timeText by remember { mutableStateOf(currentTimeText()) }

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

    LaunchedEffect(Unit) {
        while (true) {
            timeText = currentTimeText()
            delay(1000L)
        }
    }

    Surface(
        modifier = modifier
            .size(64.dp)
            .clickable {
                try {
                    context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        color = Color.Black,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fontSizePx = 14.sp.toPx()

            drawIntoCanvas { canvas ->
                val nativePaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = fontSizePx
                    typeface = resolvedTypeface
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val x = size.width / 2f
                val y = size.height / 2f - (nativePaint.descent() + nativePaint.ascent()) / 2f

                // 1. 白い外枠（STROKE）
                nativePaint.style = android.graphics.Paint.Style.STROKE
                nativePaint.strokeWidth = 3.dp.toPx()
                nativePaint.color = Color.White.toArgb()
                canvas.nativeCanvas.drawText(timeText, x, y, nativePaint)

                // 2. 黒い太文字本体（FILL）
                nativePaint.style = android.graphics.Paint.Style.FILL
                nativePaint.color = Color.Black.toArgb()
                canvas.nativeCanvas.drawText(timeText, x, y, nativePaint)
            }
        }
    }
}