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
 * アプリ一覧（ドロワー）のアイコンを長押しした時のメニュー
 */
@Composable
fun DrawerActionMenuDialog(
    appLabel: String,
    currentFolderName: String?,
    onRename: () -> Unit,
    onMoveToFolder: () -> Unit,
    onRemoveFromFolder: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "$appLabel の操作") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = "名前・アイコンを変更する",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRename() }
                            .padding(16.dp)
                    )
                }
                item {
                    Text(
                        text = if (currentFolderName != null) "別のフォルダに移動する" else "フォルダに入れる",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMoveToFolder() }
                            .padding(16.dp)
                    )
                }
                if (currentFolderName != null) {
                    item {
                        Text(
                            text = "フォルダから出す（現在：$currentFolderName）",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRemoveFromFolder() }
                                .padding(16.dp)
                        )
                    }
                }
                item {
                    Text(
                        text = "アンインストールする",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUninstall() }
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
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