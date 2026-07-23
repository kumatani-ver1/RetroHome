package com.retro.retrohome

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.retro.retrohome.component.ActionMenuDialog
import com.retro.retrohome.component.AppSelectDialog
import com.retro.retrohome.component.RenameDialog
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.screen.HomeScreen
import com.retro.retrohome.ui.theme.RetroHomeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★追加：システムバー（ナビゲーションバー・ステータスバー）の色設定
        window.navigationBarColor = android.graphics.Color.BLACK
        // アイコンを白く表示（ダークモード対応）
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            RetroHomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val installedApps = remember { getInstalledApps() }

                    val slotPreferences = remember { SlotPreferences(this) }
                    val navButtonPreferences = remember { NavButtonPreferences(this) }

                    val totalSlotCount = 40
                    val initialSlots = remember { slotPreferences.loadSlots(totalSlotCount, installedApps) }
                    val appSlots = remember { mutableStateListOf<AppIcon?>().apply { addAll(initialSlots) } }

                    var leftButtonIconUri by remember { mutableStateOf(navButtonPreferences.getLeftButtonIconUri()) }
                    var rightButtonIconUri by remember { mutableStateOf(navButtonPreferences.getRightButtonIconUri()) }

                    var selectingForLeftButton by remember { mutableStateOf(true) }
                    var showNavButtonDialog by remember { mutableStateOf(false) }

                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            try {
                                contentResolver.takePersistableUriPermission(
                                    it,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            val uriString = it.toString()
                            if (selectingForLeftButton) {
                                leftButtonIconUri = uriString
                                navButtonPreferences.saveLeftButtonIconUri(uriString)
                            } else {
                                rightButtonIconUri = uriString
                                navButtonPreferences.saveRightButtonIconUri(uriString)
                            }
                        }
                    }

                    val fontPreferences = remember { FontPreferences(this) }
                    var currentFont by remember { mutableStateOf(fontPreferences.currentFont.value) }
                    var showFontDialog by remember { mutableStateOf(false) }

                    var selectedSlotIndex by remember { mutableIntStateOf(-1) }
                    var showAppSelectDialog by remember { mutableStateOf(false) }
                    var showActionMenuDialog by remember { mutableStateOf(false) }
                    var showRenameDialog by remember { mutableStateOf(false) }

                    HomeScreen(
                        appSlots = appSlots,
                        appIcons = installedApps,
                        currentFont = currentFont,
                        leftIconUri = leftButtonIconUri,
                        rightIconUri = rightButtonIconUri,
                        onSlotClick = { index ->
                            val app = appSlots.getOrNull(index)
                            if (app != null) {
                                launchApp(app.packageName)
                            } else {
                                selectedSlotIndex = index
                                showAppSelectDialog = true
                            }
                        },
                        onSlotLongClick = { index ->
                            selectedSlotIndex = index
                            showActionMenuDialog = true
                        },
                        onLongClickIcon = { app -> },
                        onLeftButtonLongClick = {
                            selectingForLeftButton = true
                            showNavButtonDialog = true
                        },
                        onRightButtonLongClick = {
                            selectingForLeftButton = false
                            showNavButtonDialog = true
                        }
                    )

                    if (showNavButtonDialog) {
                        AlertDialog(
                            onDismissRequest = { showNavButtonDialog = false },
                            title = { Text("ボタンアイコンの設定") },
                            text = { Text("ギャラリーから選択するか、デフォルトに戻します。") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showNavButtonDialog = false
                                        galleryLauncher.launch("image/*")
                                    }
                                ) {
                                    Text("画像を選択")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showNavButtonDialog = false
                                        if (selectingForLeftButton) {
                                            leftButtonIconUri = null
                                            navButtonPreferences.saveLeftButtonIconUri(null)
                                        } else {
                                            rightButtonIconUri = null
                                            navButtonPreferences.saveRightButtonIconUri(null)
                                        }
                                    }
                                ) {
                                    Text("デフォルトに戻す")
                                }
                            }
                        )
                    }

                    if (showAppSelectDialog) {
                        AppSelectDialog(
                            installedApps = installedApps,
                            onAppSelected = { app ->
                                if (selectedSlotIndex in appSlots.indices) {
                                    appSlots[selectedSlotIndex] = app
                                    slotPreferences.saveSlots(appSlots)
                                }
                                showAppSelectDialog = false
                            },
                            onDismiss = { showAppSelectDialog = false }
                        )
                    }

                    if (showActionMenuDialog) {
                        ActionMenuDialog(
                            appLabel = appSlots.getOrNull(selectedSlotIndex)?.label ?: "",
                            onChangeApp = {
                                showActionMenuDialog = false
                                showAppSelectDialog = true
                            },
                            onRename = {
                                showActionMenuDialog = false
                                showRenameDialog = true
                            },
                            onChangeIcon = {
                                showActionMenuDialog = false
                                showRenameDialog = true
                            },
                            onChangeFont = {
                                showActionMenuDialog = false
                                showFontDialog = true
                            },
                            onRemove = {
                                if (selectedSlotIndex in appSlots.indices) {
                                    appSlots[selectedSlotIndex] = null
                                    slotPreferences.saveSlots(appSlots)
                                }
                                showActionMenuDialog = false
                            },
                            onDismiss = { showActionMenuDialog = false }
                        )
                    }

                    if (showRenameDialog) {
                        val currentApp = appSlots.getOrNull(selectedSlotIndex)
                        RenameDialog(
                            currentLabel = currentApp?.label ?: "",
                            currentIconUri = null,
                            onConfirm = { newLabel, newIconUri ->
                                if (selectedSlotIndex in appSlots.indices && currentApp != null) {
                                    appSlots[selectedSlotIndex] = currentApp.copy(label = newLabel)
                                    slotPreferences.saveSlots(appSlots)
                                }
                                showRenameDialog = false
                            },
                            onDismiss = { showRenameDialog = false }
                        )
                    }

                    if (showFontDialog) {
                        FontSelectionDialog(
                            currentFont = currentFont,
                            onFontSelected = { newFont ->
                                currentFont = newFont
                                fontPreferences.saveFont(newFont)
                                showFontDialog = false
                            },
                            onDismissRequest = { showFontDialog = false }
                        )
                    }
                }
            }
        }
    }

    private fun getInstalledApps(): List<AppIcon> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return resolveInfos.mapNotNull { info ->
            val packageName = info.activityInfo.packageName
            if (packageName == this.packageName) return@mapNotNull null

            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)
            AppIcon(packageName = packageName, label = label, icon = icon)
        }.sortedBy { it.label }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "アプリを起動できませんでした", Toast.LENGTH_SHORT).show()
        }
    }
}