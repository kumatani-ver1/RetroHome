package com.retro.retrohome.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.retrohome.AppFont
import com.retro.retrohome.model.AppIcon

/**
 * アプリアイコンを4列のグリッドで並べて表示する。
 * フォルダに入っているアプリはフォルダタイルにまとめ、それ以外を通常表示。
 * システムアプリ（アンインストール不可）は下に別セクションとして表示する。
 */
@Composable
fun IconGrid(
    appIcons: List<AppIcon>,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
    currentFont: AppFont? = null,
    folderMap: Map<String, List<String>> = emptyMap(),
    onFolderClick: (String, List<AppIcon>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    val folderedPackageNames = folderMap.values.flatten().toSet()
    val unfolderedApps = appIcons.filter { it.packageName !in folderedPackageNames }
    val regularApps = unfolderedApps.filter { !it.isSystemApp }
    val systemApps = unfolderedApps.filter { it.isSystemApp }

    fun launch(appIcon: AppIcon) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(appIcon.packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(top = topPadding)
    ) {
        if (folderMap.isNotEmpty()) {
            items(folderMap.keys.toList()) { folderName ->
                val folderApps = appIcons.filter { it.packageName in (folderMap[folderName] ?: emptyList()) }
                FolderTile(
                    folderName = folderName,
                    appCount = folderApps.size,
                    onClick = { onFolderClick(folderName, folderApps) }
                )
            }
        }

        items(regularApps) { appIcon ->
            IconItem(
                appIcon = appIcon,
                onClick = { launch(it) },
                onLongClick = onLongClickIcon,
                currentFont = currentFont
            )
        }

        if (systemApps.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "システムアプリ（アンインストール不可）",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            items(systemApps) { appIcon ->
                IconItem(
                    appIcon = appIcon,
                    onClick = { launch(it) },
                    onLongClick = onLongClickIcon,
                    currentFont = currentFont
                )
            }
        }
    }
}