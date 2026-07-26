package com.retro.retrohome.component

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.retro.retrohome.AppFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// 選んだフォルダの中の.txtファイルを全部読み込み、1行ずつをメッセージとして集める
private fun listMessages(context: android.content.Context, treeUri: Uri): List<String> {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
    val messages = mutableListOf<String>()
    root.listFiles()
        .filter { it.isFile && it.name?.endsWith(".txt", ignoreCase = true) == true }
        .forEach { file ->
            try {
                context.contentResolver.openInputStream(file.uri)?.use { stream ->
                    stream.bufferedReader().forEachLine { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty()) messages.add(trimmed)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    return messages
}

/**
 * ホーム画面に固定表示する「一言メッセージ」ウィジェット。
 * タップ：未選択→フォルダ選択／選択済み→次のランダムメッセージへ。
 * 長押し：表示間隔の設定ダイアログを開く。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageWidget(
    folderUri: Uri?,
    onRequestFolderPicker: () -> Unit,
    intervalSeconds: Int = 30,
    currentFont: AppFont? = null,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentMessage by remember { mutableStateOf<String?>(null) }

    // フォルダが変わるたびに、中の.txtを読み直してランダムに1つ選ぶ
    LaunchedEffect(folderUri) {
        if (folderUri != null) {
            val loaded = withContext(Dispatchers.IO) { listMessages(context, folderUri) }
            messages = loaded
            currentMessage = loaded.randomOrNull()
        } else {
            messages = emptyList()
            currentMessage = null
        }
    }

    // 設定された間隔ごとに自動でランダムなメッセージに切り替える
    LaunchedEffect(folderUri, intervalSeconds) {
        if (folderUri == null) return@LaunchedEffect
        while (true) {
            delay(intervalSeconds * 1000L)
            if (messages.isNotEmpty()) {
                currentMessage = messages.random()
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = {
                    if (folderUri == null) {
                        onRequestFolderPicker()
                    } else if (messages.isNotEmpty()) {
                        currentMessage = messages.random()
                    }
                },
                onLongClick = onLongClick
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentMessage
                    ?: if (folderUri == null) "タップしてメッセージフォルダを選択" else "メッセージが見つかりません",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = currentFont?.fontFamily ?: FontFamily.Default,
                textAlign = TextAlign.Center
            )
        }
    }
}