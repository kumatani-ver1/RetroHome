package com.retro.retrohome

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.retro.retrohome.component.IntervalSettingDialog
import com.retro.retrohome.component.RenameDialog
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.screen.HomeScreen
import com.retro.retrohome.ui.theme.RetroHomeTheme
import com.retro.retrohome.PhotoWidgetPreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // システムバー（ナビゲーションバー・ステータスバー）の色設定
        window.navigationBarColor = Color.BLACK
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            RetroHomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val customPrefs = remember { AppCustomPreferences(this) }
                    val initialApps = remember { getInstalledApps(customPrefs) }
                    val installedApps = remember { mutableStateListOf<AppIcon>().apply { addAll(initialApps) } }

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
                    val photoWidgetPreferences = remember { PhotoWidgetPreferences(this) }
                    var photoWidgetFolderUri by remember { mutableStateOf(photoWidgetPreferences.folderUri.value) }
                    var photoWidgetIntervalSeconds by remember { mutableStateOf(photoWidgetPreferences.intervalSeconds.value) }
                    var showPhotoIntervalDialog by remember { mutableStateOf(false) }
                    val photoFolderPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocumentTree()
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
                            photoWidgetPreferences.saveFolderUri(it)
                            photoWidgetFolderUri = it
                        }
                    }

                    var selectedSlotIndex by remember { mutableIntStateOf(-1) }
                    var showAppSelectDialog by remember { mutableStateOf(false) }
                    var showActionMenuDialog by remember { mutableStateOf(false) }
                    var showRenameDialog by remember { mutableStateOf(false) }

                    var selectedDrawerApp by remember { mutableStateOf<AppIcon?>(null) }
                    var showDrawerRenameDialog by remember { mutableStateOf(false) }

                    HomeScreen(
                        appSlots = appSlots,
                        appIcons = installedApps,
                        currentFont = currentFont,
                        leftIconUri = leftButtonIconUri,
                        rightIconUri = rightButtonIconUri,
                       photoWidgetFolderUri = photoWidgetFolderUri,
                        onRequestPhotoFolderPicker = { photoFolderPickerLauncher.launch(null) },
                        onRequestPhotoIntervalSetting = { showPhotoIntervalDialog = true },
                        photoWidgetIntervalSeconds = photoWidgetIntervalSeconds,

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
                        onLongClickIcon = { app ->
                            selectedDrawerApp = app
                            showDrawerRenameDialog = true
                        },
                        onLeftButtonLongClick = {
                            selectingForLeftButton = true
                            showNavButtonDialog = true
                        },
                        onRightButtonLongClick = {
                            selectingForLeftButton = false
                            showNavButtonDialog = true
                        }
                    )

// 画像ウィジェットの間隔設定ダイアログ
                    if (showPhotoIntervalDialog) {
                        IntervalSettingDialog(
                            currentTotalSeconds = photoWidgetIntervalSeconds,
                            onConfirm = { seconds ->
                                photoWidgetPreferences.saveIntervalSeconds(seconds)
                                photoWidgetIntervalSeconds = seconds
                                showPhotoIntervalDialog = false
                            },
                            onDismiss = { showPhotoIntervalDialog = false }
                        )
                    }
// 画像ウィジェットの間隔設定ダイアログ
                    if (showPhotoIntervalDialog) {
                        IntervalSettingDialog(
                            currentTotalSeconds = photoWidgetIntervalSeconds,
                            onConfirm = { seconds ->
                                photoWidgetPreferences.saveIntervalSeconds(seconds)
                                photoWidgetIntervalSeconds = seconds
                                showPhotoIntervalDialog = false
                            },
                            onDismiss = { showPhotoIntervalDialog = false }
                        )
                    }
                    // ナビボタンの画像変更ダイアログ
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

                    // 空きスロット選択時のアプリ割り当てダイアログ
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

                    // ホームスロット長押し時のアクションメニュー
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

                    // ホーム画面のスロット用リネームダイアログ
                    if (showRenameDialog) {
                        val currentApp = appSlots.getOrNull(selectedSlotIndex)
                        RenameDialog(
                            currentLabel = currentApp?.label ?: "",
                            currentIconUri = currentApp?.iconUri,
                            onConfirm = { newLabel, newIconUri ->
                                if (selectedSlotIndex in appSlots.indices && currentApp != null) {
                                    if (!newIconUri.isNullOrEmpty()) {
                                        try {
                                            contentResolver.takePersistableUriPermission(
                                                Uri.parse(newIconUri),
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    val updatedApp = currentApp.copy(
                                        label = newLabel,
                                        iconUri = newIconUri
                                    )

                                    // ホーム画面を更新＆保存
                                    appSlots[selectedSlotIndex] = updatedApp
                                    slotPreferences.saveSlots(appSlots)

                                    // カスタム情報を保存
                                    customPrefs.saveLabel(currentApp.packageName, newLabel)
                                    customPrefs.saveIconUri(currentApp.packageName, newIconUri)

                                    // ドロワー（アプリ一覧）側も連動更新
                                    val drawerIndex = installedApps.indexOfFirst { it.packageName == currentApp.packageName }
                                    if (drawerIndex != -1) {
                                        installedApps[drawerIndex] = updatedApp
                                    }
                                }
                                showRenameDialog = false
                            },
                            onDismiss = { showRenameDialog = false }
                        )
                    }

                    // ドロワー（アプリ一覧）用リネームダイアログ
                    if (showDrawerRenameDialog && selectedDrawerApp != null) {
                        val targetApp = selectedDrawerApp!!
                        RenameDialog(
                            currentLabel = targetApp.label,
                            currentIconUri = targetApp.iconUri,
                            onConfirm = { newLabel, newIconUri ->
                                if (!newIconUri.isNullOrEmpty()) {
                                    try {
                                        contentResolver.takePersistableUriPermission(
                                            Uri.parse(newIconUri),
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                // 1. Preferences に保存
                                customPrefs.saveLabel(targetApp.packageName, newLabel)
                                customPrefs.saveIconUri(targetApp.packageName, newIconUri)

                                val updatedApp = targetApp.copy(
                                    label = newLabel,
                                    iconUri = newIconUri
                                )

                                // 2. ドロワー一覧（installedApps）を更新
                                val drawerIndex = installedApps.indexOfFirst { it.packageName == targetApp.packageName }
                                if (drawerIndex != -1) {
                                    installedApps[drawerIndex] = updatedApp
                                }

                                // 3. ホーム画面に配置されている同じアプリも連動更新して保存
                                appSlots.indices.forEach { i ->
                                    if (appSlots[i]?.packageName == targetApp.packageName) {
                                        appSlots[i] = updatedApp
                                    }
                                }
                                slotPreferences.saveSlots(appSlots)

                                showDrawerRenameDialog = false
                                selectedDrawerApp = null
                            },
                            onDismiss = {
                                showDrawerRenameDialog = false
                                selectedDrawerApp = null
                            }
                        )
                    }

                    // フォント選択ダイアログ
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

    // アプリ取得時に保存されたカスタム情報を読み出す
    private fun getInstalledApps(customPrefs: AppCustomPreferences): List<AppIcon> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return resolveInfos.mapNotNull { info ->
            val packageName = info.activityInfo.packageName
            if (packageName == this.packageName) return@mapNotNull null

            val originalLabel = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)

            val label = customPrefs.getLabel(packageName, originalLabel)
            val iconUri = customPrefs.getIconUri(packageName)

            AppIcon(
                packageName = packageName,
                label = label,
                icon = icon,
                iconUri = iconUri
            )
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