package com.retro.retrohome.screen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.retrohome.AppFont
import com.retro.retrohome.component.PageNavButton
import com.retro.retrohome.component.ClockWidget
import com.retro.retrohome.component.DockAppShortcut
import com.retro.retrohome.component.PhotoWidget
import com.retro.retrohome.component.WeatherWidget
import com.retro.retrohome.component.MessageWidget
import com.retro.retrohome.component.RetroAppItem
import com.retro.retrohome.model.AppIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    appSlots: List<AppIcon?>,
    appIcons: List<AppIcon>,
    currentFont: AppFont,
    onSlotClick: (Int) -> Unit,
    onSlotLongClick: (Int) -> Unit,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier,
    leftButtonText: String = "◀",
    rightButtonText: String = "▶",
    leftIconUri: String? = null,
    rightIconUri: String? = null,
    onLeftButtonLongClick: () -> Unit = {},
    onRightButtonLongClick: () -> Unit = {},
    photoWidgetFolderUri: Uri? = null,
    onRequestPhotoFolderPicker: () -> Unit = {},
    photoWidgetIntervalSeconds: Int = 30,
    onRequestPhotoIntervalSetting: () -> Unit = {},
    weatherLatitude: Double? = null,
    weatherLongitude: Double? = null,
    onRequestLocationPicker: () -> Unit = {},
    messageWidgetFolderUri: Uri? = null,
    onRequestMessageFolderPicker: () -> Unit = {},
    messageWidgetIntervalSeconds: Int = 30,
    onRequestMessageIntervalSetting: () -> Unit = {},
    onPageNumberLongClick: () -> Unit = {},
    folderMap: Map<String, List<String>> = emptyMap(),
    onFolderClick: (String, List<AppIcon>) -> Unit = { _, _ -> },
    registerHomeButtonHandler: (() -> Unit) -> Unit = {}
) {
    var openProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        registerHomeButtonHandler { openProgress = 0f }
    }
    BackHandler(enabled = openProgress > 0f) {
        openProgress = 0f
    }
    var currentPage by remember { mutableIntStateOf(0) }
    var isForwarding by remember { mutableStateOf(true) }

    val slotsPerPage = 8
    val maxPages = (appSlots.size + slotsPerPage - 1) / slotsPerPage.coerceAtLeast(1)
    val totalPages = if (maxPages < 1) 1 else maxPages

    val animatedProgress by animateFloatAsState(
        targetValue = openProgress,
        animationSpec = tween(durationMillis = 200),
        label = "drawerOpenProgress"
    )

    // ラベル（RetroAppItem）と同じフォント解像度の仕組み
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val resolvedTypeface = remember(currentFont, fontFamilyResolver) {
        val family = currentFont.fontFamily
        val result = fontFamilyResolver.resolve(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        )
        result.value as? android.graphics.Typeface
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val fullHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val dragSensitivityPx = fullHeightPx / 3f

        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (isForwarding) {
                    (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } + fadeOut())
                } else {
                    (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeOut())
                }
            },
            label = "pageTransition",
            modifier = Modifier.pointerInput(totalPages) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        val threshold = size.width / 4f
                        if (totalDrag <= -threshold) {
                            // 左にスワイプ → 次のページへ
                            isForwarding = true
                            currentPage = if (currentPage < totalPages - 1) currentPage + 1 else 0
                        } else if (totalDrag >= threshold) {
                            // 右にスワイプ → 前のページへ
                            isForwarding = false
                            currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1
                        }
                    }
                )
            }
        ) { page ->
            val startIndex = page * slotsPerPage

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                userScrollEnabled = false,
                contentPadding = PaddingValues(
                    top = 480.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 160.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(slotsPerPage) { indexOnPage ->
                    val actualSlotIndex = startIndex + indexOnPage
                    val appIcon = appSlots.getOrNull(actualSlotIndex)
                    RetroAppItem(
                        appIcon = appIcon,
                        currentFont = currentFont,
                        onClick = { onSlotClick(actualSlotIndex) },
                        onLongClick = { onSlotLongClick(actualSlotIndex) }
                    )
                }
            }
        }

        // 画像ウィジェット（ページ遷移の対象外＝AnimatedContentの外に置くことで固定表示にする）
        PhotoWidget(
            folderUri = photoWidgetFolderUri,
            onRequestFolderPicker = onRequestPhotoFolderPicker,
            intervalSeconds = photoWidgetIntervalSeconds,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 60.dp),
            onLongClick = onRequestPhotoIntervalSetting
        )

        // 天気予報ウィジェット（画像ウィジェットの下に固定表示）
        WeatherWidget(
            latitude = weatherLatitude,
            longitude = weatherLongitude,
            onRequestLocationPicker = onRequestLocationPicker,
            currentFont = currentFont,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 400.dp)
        )

        // メッセージウィジェット（天気予報ウィジェットの下に固定表示）
        MessageWidget(
            folderUri = messageWidgetFolderUri,
            onRequestFolderPicker = onRequestMessageFolderPicker,
            intervalSeconds = messageWidgetIntervalSeconds,
            currentFont = currentFont,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 320.dp),
            onLongClick = onRequestMessageIntervalSetting
        )

        // 左右のページ送りボタン ＆ 中央のページ番号（袋文字）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 65.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左ボタン
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PageNavButton(
                    text = leftButtonText,
                    iconUri = leftIconUri,
                    onClick = {
                        isForwarding = false
                        if (currentPage > 0) {
                            currentPage--
                        } else {
                            currentPage = totalPages - 1
                        }
                    },
                    onLongClick = onLeftButtonLongClick
                )
            }

            // 時計ウィジェット（左ボタンとページ番号の間）
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ClockWidget(currentFont = currentFont)
            }

            // 中央：ページ番号表示エリア（RetroAppItemと同様の袋文字描画／長押しで設定メニュー）
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = onPageNumberLongClick
                        )
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val pageText = "${currentPage + 1} / $totalPages"

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val fontSizePx = 14.sp.toPx()

                        drawIntoCanvas { canvas ->
                            val nativePaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                textSize = fontSizePx
                                typeface = resolvedTypeface
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }

                            val x = size.width / 2f
                            val y = size.height / 2f - (nativePaint.descent() + nativePaint.ascent()) / 2f

                            // 1. 白い外枠（STROKE）
                            nativePaint.style = android.graphics.Paint.Style.STROKE
                            nativePaint.strokeWidth = 4.dp.toPx()
                            nativePaint.color = Color.White.toArgb()
                            canvas.nativeCanvas.drawText(pageText, x, y, nativePaint)

                            // 2. 黒い太文字本体（FILL）
                            nativePaint.style = android.graphics.Paint.Style.FILL
                            nativePaint.color = Color.Black.toArgb()
                            canvas.nativeCanvas.drawText(pageText, x, y, nativePaint)
                        }
                    }
                }
            }

            // お気に入りアプリのショートカット（ページ番号と右ボタンの間）
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DockAppShortcut(installedApps = appIcons)
            }

            // 右ボタン
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PageNavButton(
                    text = rightButtonText,
                    iconUri = rightIconUri,
                    onClick = {
                        isForwarding = true
                        if (currentPage < totalPages - 1) {
                            currentPage++
                        } else {
                            currentPage = 0
                        }
                    },
                    onLongClick = onRightButtonLongClick
                )
            }
        }

        // アプリドロワー検出エリア
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
                .pointerInput(dragSensitivityPx) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            openProgress = (openProgress - dragAmount / dragSensitivityPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            openProgress = if (openProgress > 0.5f) 1f else 0f
                        }
                    )
                }
        )

        // アプリドロワー画面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    val yOffset = ((1f - animatedProgress) * fullHeightPx).roundToInt()
                    IntOffset(0, yOffset)
                }
        ) {
            AppDrawerScreen(
                appIcons = appIcons,
                folderMap = folderMap,
                onFolderClick = onFolderClick,
                onDrag = { dragAmount ->
                    openProgress = (openProgress - dragAmount / dragSensitivityPx).coerceIn(0f, 1f)
                },
                onDragEnd = {
                    openProgress = if (openProgress > 0.5f) 1f else 0f
                },
                onLongClickIcon = onLongClickIcon,
                currentFont = currentFont
            )
        }
    }
}