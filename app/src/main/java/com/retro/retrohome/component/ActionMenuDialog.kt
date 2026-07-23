package com.retro.retrohome.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ホーム画面の枠（アプリセット済み）を長押しした時に表示するメニュー
 */
@Composable
fun ActionMenuDialog(
    appLabel: String,
    onChangeApp: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
    onChangeFont: () -> Unit, // ★追加：フォント変更用
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "$appLabel の設定") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "アプリを変更する",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangeApp() }
                            .padding(16.dp)
                    )
                }
                item {
                    Text(
                        text = "名前（ラベル）を変更する",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRename() }
                            .padding(16.dp)
                    )
                }
                item {
                    Text(
                        text = "アイコンを変更する",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangeIcon() }
                            .padding(16.dp)
                    )
                }
                item {
                    Text(
                        text = "フォントを変更する", // ★追加
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangeFont() }
                            .padding(16.dp)
                    )
                }
                item {
                    Text(
                        text = "枠から外す（EMPTYにする）",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRemove() }
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error // 赤色にして注意を引く
                    )
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