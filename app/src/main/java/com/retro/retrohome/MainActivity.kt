package com.retro.retrohome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.ui.component.AppSelectDialog
import com.retro.retrohome.ui.component.RenameDialog
import com.retro.retrohome.ui.screen.HomeScreen
import com.retro.retrohome.ui.theme.RetroHomeTheme
import com.retro.retrohome.util.InstalledAppsProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetroHomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current // アプリ起動に使うためContextを取得
                    val installedApps = InstalledAppsProvider.getInstalledApps(applicationContext)

                    var customLabels by remember { mutableStateOf(mapOf<String, String>()) }
                    var editingIcon by remember { mutableStateOf<AppIcon?>(null) }

                    // 10個のインベントリスロット
                    var appSlots by remember { mutableStateOf<List<AppIcon?>>(List(10) { null }) }
                    // 「今どの枠（何番目）のアプリを選ぼうとしているか」を記憶（nullならダイアログ非表示）
                    var selectingSlotIndex by remember { mutableStateOf<Int?>(null) }

                    val displayedApps = installedApps.map { appIcon ->
                        val customLabel = customLabels[appIcon.packageName]
                        if (customLabel != null) {
                            appIcon.copy(label = customLabel)
                        } else {
                            appIcon
                        }
                    }

                    HomeScreen(
                        appSlots = appSlots,
                        appIcons = displayedApps,
                        onSlotClick = { slotIndex ->
                            // 【短押し】枠にアプリが入っていれば起動する
                            val selectedApp = appSlots[slotIndex]
                            if (selectedApp != null) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(selectedApp.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                }
                            }
                        },
                        onSlotLongClick = { slotIndex ->
                            // 【長押し】アプリ選択ダイアログを開く
                            selectingSlotIndex = slotIndex
                        },
                        onLongClickIcon = { longPressedIcon ->
                            editingIcon = longPressedIcon
                        }
                    )

                    // アプリ選択ダイアログの表示
                    selectingSlotIndex?.let { slotIndex ->
                        AppSelectDialog(
                            installedApps = displayedApps,
                            onAppSelected = { selectedApp ->
                                // 選ばれたアプリを該当する枠にセットする
                                val newSlots = appSlots.toMutableList()
                                newSlots[slotIndex] = selectedApp
                                appSlots = newSlots

                                // ダイアログを閉じる
                                selectingSlotIndex = null
                            },
                            onDismiss = {
                                selectingSlotIndex = null
                            }
                        )
                    }

                    // 名前変更ダイアログの表示
                    editingIcon?.let { icon ->
                        RenameDialog(
                            currentLabel = icon.label,
                            onConfirm = { newLabel ->
                                customLabels = customLabels + (icon.packageName to newLabel)
                                editingIcon = null
                            },
                            onDismiss = {
                                editingIcon = null
                            }
                        )
                    }
                }
            }
        }
    }
}