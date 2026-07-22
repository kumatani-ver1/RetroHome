package com.retro.retrohome.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.retro.retrohome.model.AppIcon

@Composable
fun AppSelectDialog(
    installedApps: List<AppIcon>,
    onAppSelected: (AppIcon?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "セットするアプリを選択") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                item {
                    Text(
                        text = "（なし）",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(null) }
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                items(installedApps) { app ->
                    // アイコンとテキストを横並びに配置
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(app) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        app.icon?.let { drawable ->
                            Image(
                                painter = rememberDrawablePainter(drawable = drawable),
                                contentDescription = app.label,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp)) // アイコンと文字の間の隙間
                        Text(text = app.label)
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