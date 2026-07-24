package com.retro.retrohome.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.retrohome.AppFont
import com.retro.retrohome.component.IconGrid
import com.retro.retrohome.model.AppIcon

@Composable
fun AppDrawerScreen(
    appIcons: List<AppIcon>,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onLongClickIcon: (AppIcon) -> Unit,
    modifier: Modifier = Modifier,
    currentFont: AppFont? = null
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(appIcons, searchQuery) {
        if (searchQuery.isBlank()) {
            appIcons
        } else {
            appIcons.filter { it.label.contains(searchQuery, ignoreCase = true) }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero

                if (dragOffset > 0f && available.y < 0f) {
                    val consumeY = if (dragOffset + available.y < 0f) {
                        -dragOffset
                    } else {
                        available.y
                    }
                    dragOffset += consumeY
                    onDrag(consumeY)
                    return Offset(0f, consumeY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0f) {
                    dragOffset += available.y
                    onDrag(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffset > 0f) {
                    onDragEnd()
                    dragOffset = 0f
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 16.dp, bottom = 16.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                color = Color.Black,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, Color.White),
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "現在のアプリ ${appIcons.size}個",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                IconGrid(
                    appIcons = filteredApps,
                    onLongClickIcon = onLongClickIcon,
                    currentFont = currentFont
                )
            }
        }
    }
}