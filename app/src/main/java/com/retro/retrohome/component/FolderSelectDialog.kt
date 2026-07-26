package com.retro.retrohome.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 「フォルダに入れる」時に、既存フォルダを選ぶか、新規フォルダ名を入力するダイアログ
 */
@Composable
fun FolderSelectDialog(
    existingFolderNames: List<String>,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("フォルダに入れる") },
        text = {
            Column {
                if (existingFolderNames.isNotEmpty()) {
                    Text("既存のフォルダ")
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(existingFolderNames) { name ->
                            Text(
                                text = name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFolderSelected(name) }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("新しいフォルダ名") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onFolderSelected(newFolderName.trim())
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("この名前で新規作成して入れる")
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