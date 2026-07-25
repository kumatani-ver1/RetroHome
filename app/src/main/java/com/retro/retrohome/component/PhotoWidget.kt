package com.retro.retrohome.component

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// 取り込み対象の拡張子（JPG/PNG/WEBP。GIF・HEICは対象外）
private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

private fun listImageUris(context: android.content.Context, treeUri: Uri): List<Uri> {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
    return root.listFiles()
        .filter { file ->
            file.isFile && (file.name?.substringAfterLast('.', "")?.lowercase() in ALLOWED_IMAGE_EXTENSIONS)
        }
        .map { it.uri }
}

/**
 * ホーム画面上部に固定表示する「画像ウィジェット」の枠。
 * STEP2: フォルダを選んで、中の画像（JPG/PNG/WEBP）をランダム表示する。
 * タップで「フォルダ未選択→選択」「選択済み→次のランダム画像」を切り替える。
 * 長押しは表示間隔の設定用に予約（STEP3で実装）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoWidget(
    folderUri: Uri?,
    onRequestFolderPicker: () -> Unit,
    intervalSeconds: Int = 30,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }

    // フォルダが変わるたびに、中の画像一覧を読み直してランダムに1枚選ぶ
    LaunchedEffect(folderUri) {
        if (folderUri != null) {
            val uris = withContext(Dispatchers.IO) { listImageUris(context, folderUri) }
            imageUris = uris
            currentImageUri = uris.randomOrNull()
        } else {
            imageUris = emptyList()
            currentImageUri = null
        }
    }

    // 設定された間隔（秒）ごとに、自動でランダムな画像に切り替える
    LaunchedEffect(folderUri, intervalSeconds) {
        if (folderUri == null) return@LaunchedEffect
        while (true) {
            delay(intervalSeconds * 1000L)
            if (imageUris.isNotEmpty()) {
                currentImageUri = imageUris.random()
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = {
                    if (folderUri == null) {
                        onRequestFolderPicker()
                    } else if (imageUris.isNotEmpty()) {
                        currentImageUri = imageUris.random()
                    }
                },
                onLongClick = onLongClick
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentImageUri != null -> {
                    AsyncImage(
                        model = currentImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                folderUri == null -> {
                    Text(
                        text = "タップして画像フォルダを選択",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                else -> {
                    Text(
                        text = "対応する画像（JPG/PNG/WEBP）が見つかりません",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}