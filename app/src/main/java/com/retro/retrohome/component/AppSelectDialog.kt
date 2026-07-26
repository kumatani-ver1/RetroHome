package com.retro.retrohome.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.retro.retrohome.model.AppIcon

/**
 * ホーム画面の枠にセットするアプリを選ぶダイアログ（検索バー＋フォルダ分け表示）
 */
@Composable
fun AppSelectDialog(
    installedApps: List<AppIcon>,
    onAppSelected: (AppIcon?) -> Unit,
    onDismiss: () -> Unit,
    folderMap: Map<String, List<String>> = emptyMap()
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
        }
    }

    // 検索中はフォルダ分けを解除してフラット表示
    val effectiveFolderMap = if (searchQuery.isBlank()) folderMap else emptyMap()
    val folderedPackageNames = effectiveFolderMap.values.flatten().toSet()
    val unfolderedApps = filteredApps.filter { it.packageName !in folderedPackageNames }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "アプリを選択") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("アプリ名で検索") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    // (なし) に戻す選択肢
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(null) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "（枠を空にする）")
                        }
                    }

                    effectiveFolderMap.forEach { (folderName, packageNames) ->
                        val appsInFolder = filteredApps.filter { it.packageName in packageNames }
                        if (appsInFolder.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📁 $folderName",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp, start = 12.dp)
                                )
                            }
                            items(appsInFolder) { app ->
                                AppSelectItemRow(app = app, onClick = { onAppSelected(app) })
                            }
                        }
                    }

                    items(unfolderedApps) { app ->
                        AppSelectItemRow(
                            app = app,
                            onClick = { onAppSelected(app) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun AppSelectItemRow(
    app: AppIcon,
    onClick: () -> Unit
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
            .clickable { onClick() }
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