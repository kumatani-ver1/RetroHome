package com.retro.retrohome.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.retro.retrohome.model.AppIcon

/**
 * ホーム画面に表示するアイコン1個分の見た目
 * 白フチの四角枠 + ラベル文字のみのシンプルな表示（STEP1時点では画像なし）
 */
@Composable
fun IconItem(
    appIcon: AppIcon,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // 正方形にする
            .padding(4.dp)
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = appIcon.label,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp)
        )
    }
}