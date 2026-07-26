package com.retro.retrohome.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.retro.retrohome.model.AppIcon

/**
 * フォルダをタップした時に、中に入っているアプリを一覧表示するダイアログ
 * タップ：起動 / 長押し：既存の操作メニュー（onLongClickIconをそのまま利用）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderContentsDialog(
    folderName: String,
    apps: List<AppIcon>,
    onAppClick: (AppIcon) -> Unit,
    onAppLongClick: (AppIcon) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = folderName) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(apps) { app ->
                    FolderAppRow(
                        app = app,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderAppRow(
    app: AppIcon,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    val iconBitmap = remember(app.icon) {
        val drawable = app.icon ?: return@remember null
        val sizePx = (32 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = app.label)
    }
}