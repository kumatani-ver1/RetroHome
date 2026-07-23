package com.retro.retrohome.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.retro.retrohome.model.AppIcon

/**
 * アプリアイコンを4列のグリッドで並べて表示する
 * アイコンをタップすると、そのアプリを起動する
 * 長押しすると onLongClickIcon が呼ばれる（ラベル編集ダイアログを開くため）
 *
 * @param topPadding グリッド上部の余白（ホーム画面とアプリ一覧画面で別の値を渡せるようにしている）
 */
@Composable
fun IconGrid(
    appIcons: List<AppIcon>,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // 横4列固定
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(top = topPadding)
    ) {
        items(appIcons) { appIcon ->
            IconItem(
                appIcon = appIcon,
                onClick = { tappedIcon ->
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(tappedIcon.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    }
                },
                onLongClick = onLongClickIcon
            )
        }
    }
}