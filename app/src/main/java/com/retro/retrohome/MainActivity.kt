package com.retro.retrohome

import android.os.Bundle
import android.widget.Toast
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
import com.retro.retrohome.component.ActionMenuDialog
import com.retro.retrohome.component.AppSelectDialog
import com.retro.retrohome.component.RenameDialog
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.screen.HomeScreen
import com.retro.retrohome.ui.theme.RetroHomeTheme
import com.retro.retrohome.util.IconPreferences
import com.retro.retrohome.util.InstalledAppsProvider
import com.retro.retrohome.util.LabelPreferences
import com.retro.retrohome.util.SlotPreferences

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
                    val context = LocalContext.current
                    val installedApps = InstalledAppsProvider.getInstalledApps(context)

                    var customLabels by remember { mutableStateOf(LabelPreferences.loadLabels(context)) }
                    var customIconUris by remember { mutableStateOf(IconPreferences.loadIconUris(context)) }

                    var editingIcon by remember { mutableStateOf<AppIcon?>(null) }
                    var selectingSlotIndex by remember { mutableStateOf<Int?>(null) }
                    var actionMenuSlotIndex by remember { mutableStateOf<Int?>(null) }

                    val savedSlotData = remember { SlotPreferences.loadSlots(context) }

                    var appSlots by remember {
                        mutableStateOf<List<AppIcon?>>(
                            List(10) { index ->
                                val savedPackageName = savedSlotData[index]
                                installedApps.find { it.packageName == savedPackageName }
                            }
                        )
                    }

                    // ラベルとカスタムアイコンUriを適用したアプリ一覧
                    val displayedApps = installedApps.map { appIcon ->
                        val customLabel = customLabels[appIcon.packageName]
                        val customUri = customIconUris[appIcon.packageName]
                        appIcon.copy(
                            label = customLabel ?: appIcon.label,
                            iconUri = customUri
                        )
                    }

                    // ラベルとカスタムアイコンUriを適用したスロット一覧
                    val displayedSlots = appSlots.map { slotApp ->
                        if (slotApp != null) {
                            val customLabel = customLabels[slotApp.packageName]
                            val customUri = customIconUris[slotApp.packageName]
                            slotApp.copy(
                                label = customLabel ?: slotApp.label,
                                iconUri = customUri
                            )
                        } else {
                            null
                        }
                    }

                    HomeScreen(
                        appSlots = displayedSlots,
                        appIcons = displayedApps,
                        onSlotClick = { slotIndex ->
                            val selectedApp = displayedSlots[slotIndex]
                            if (selectedApp != null) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(selectedApp.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                }
                            }
                        },
                        onSlotLongClick = { slotIndex ->
                            if (displayedSlots[slotIndex] == null) {
                                selectingSlotIndex = slotIndex
                            } else {
                                actionMenuSlotIndex = slotIndex
                            }
                        },
                        onLongClickIcon = { longPressedIcon ->
                            editingIcon = longPressedIcon
                        }
                    )

                    actionMenuSlotIndex?.let { slotIndex ->
                        val selectedApp = displayedSlots[slotIndex]
                        if (selectedApp != null) {
                            ActionMenuDialog(
                                appLabel = selectedApp.label,
                                onChangeApp = {
                                    actionMenuSlotIndex = null
                                    selectingSlotIndex = slotIndex
                                },
                                onRename = {
                                    actionMenuSlotIndex = null
                                    editingIcon = selectedApp
                                },
                                onChangeIcon = {
                                    actionMenuSlotIndex = null
                                    editingIcon = selectedApp
                                },
                                onRemove = {
                                    val newSlots = appSlots.toMutableList()
                                    newSlots[slotIndex] = null
                                    appSlots = newSlots
                                    SlotPreferences.saveSlot(context, slotIndex, null)
                                    actionMenuSlotIndex = null
                                },
                                onDismiss = {
                                    actionMenuSlotIndex = null
                                }
                            )
                        } else {
                            actionMenuSlotIndex = null
                        }
                    }

                    selectingSlotIndex?.let { slotIndex ->
                        AppSelectDialog(
                            installedApps = displayedApps,
                            onAppSelected = { selectedApp ->
                                val newSlots = appSlots.toMutableList()
                                newSlots[slotIndex] = if (selectedApp != null) {
                                    installedApps.find { it.packageName == selectedApp.packageName }
                                } else null

                                appSlots = newSlots
                                SlotPreferences.saveSlot(context, slotIndex, selectedApp?.packageName)
                                selectingSlotIndex = null
                            },
                            onDismiss = {
                                selectingSlotIndex = null
                            }
                        )
                    }

                    editingIcon?.let { icon ->
                        RenameDialog(
                            currentLabel = icon.label,
                            currentIconUri = customIconUris[icon.packageName],
                            onConfirm = { newLabel, newIconUri ->
                                customLabels = customLabels + (icon.packageName to newLabel)
                                LabelPreferences.saveLabel(context, icon.packageName, newLabel)

                                if (newIconUri != null) {
                                    customIconUris = customIconUris + (icon.packageName to newIconUri)
                                    IconPreferences.saveIconUri(context, icon.packageName, newIconUri)
                                } else {
                                    customIconUris = customIconUris - icon.packageName
                                    IconPreferences.saveIconUri(context, icon.packageName, null)
                                }

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