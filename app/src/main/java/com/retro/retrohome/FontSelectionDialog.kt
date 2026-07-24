package com.retro.retrohome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FontSelectionDialog(
    currentFont: AppFont,
    onFontSelected: (AppFont) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "フォント選択") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(AppFont.entries) { font ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontSelected(font) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (font == currentFont),
                            onClick = { onFontSelected(font) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = font.label,
                            fontFamily = font.fontFamily,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("閉じる")
            }
        }
    )
}