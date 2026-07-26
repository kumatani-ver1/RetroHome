package com.retro.retrohome.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.retro.retrohome.DockAppShortcutPreferences
import com.retro.retrohome.model.AppIcon

/**
 * ドックに配置する「お気に入りアプリ」ショートカット（64x64）。
 * タップでそのアプリを起動、長押しで別のアプリに変更。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DockAppShortcut(
    installedApps: List<AppIcon>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { DockAppShortcutPreferences(context) }
    var selectedPackageName by remember { mutableStateOf(preferences.getPackageName()) }
    var showSelectDialog by remember { mutableStateOf(false) }

    val selectedApp = installedApps.find { it.packageName == selectedPackageName }

    // アダプティブアイコンを丸マスクなし・正方形のまま描画するためのビットマップ変換
    val iconBitmap = remember(selectedApp?.icon) {
        val drawable = selectedApp?.icon ?: return@remember null
        val sizePx = (48 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
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

    Surface(
        modifier = modifier
            .size(64.dp)
            .combinedClickable(
                onClick = {
                    if (selectedApp != null) {
                        context.packageManager.getLaunchIntentForPackage(selectedApp.packageName)?.let {
                            context.startActivity(it)
                        }
                    } else {
                        showSelectDialog = true
                    }
                },
                onLongClick = { showSelectDialog = true }
            ),
        color = Color.Black,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            iconBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = selectedApp?.label,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showSelectDialog) {
        AppSelectDialog(
            installedApps = installedApps,
            onAppSelected = { appIcon ->
                selectedPackageName = appIcon?.packageName
                preferences.savePackageName(appIcon?.packageName)
                showSelectDialog = false
            },
            onDismiss = { showSelectDialog = false }
        )
    }
}