package com.retro.retrohome.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro.retrohome.model.AppIcon

/**
 * アプリアイコンを4列のグリッドで並べて表示する
 * STEP1時点では1ページ分（最大20個）を単純に並べるだけ
 */
@Composable
fun IconGrid(
    appIcons: List<AppIcon>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // 横4列固定
        modifier = modifier.padding(8.dp)
    ) {
        items(appIcons) { appIcon ->
            IconItem(appIcon = appIcon)
        }
    }
}