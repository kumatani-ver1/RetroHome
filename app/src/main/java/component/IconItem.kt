package com.retro.retrohome.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.retro.retrohome.model.AppIcon

/**
 * ホーム画面に表示するアイコン1個分の見た目
 * 白フチの四角枠 + アイコン画像 + ラベル文字
 * タップすると onClick、長押しすると onLongClick が呼ばれる
 */
@Composable
fun IconItem(
    appIcon: AppIcon,
    onClick: (AppIcon) -> Unit,
    onLongClick: (AppIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f) // 正方形にする
            .padding(4.dp)
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground)
            )
            .combinedClickable(
                onClick = { onClick(appIcon) },
                onLongClick = { onLongClick(appIcon) }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // アイコン画像（取得できていれば表示する）
        appIcon.icon?.let { drawable ->
            Image(
                painter = rememberDrawablePainter(drawable = drawable),
                contentDescription = appIcon.label,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = appIcon.label,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}