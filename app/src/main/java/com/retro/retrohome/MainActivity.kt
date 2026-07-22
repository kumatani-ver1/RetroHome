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
import com.retro.retrohome.model.AppIcon
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
                    // 実際にインストールされているアプリ一覧（元データ）
                    val installedApps = InstalledAppsProvider.getInstalledApps(applicationContext)

                    // パッケージ名ごとに変更後のラベルを覚えておく場所
                    var customLabels by remember { mutableStateOf(mapOf<String, String>()) }

                    // 今、編集ダイアログを表示中かどうか
                    var editingIcon by remember { mutableStateOf<AppIcon?>(null) }

                    // 元データに、変更済みラベルを反映したリストを作る
                    val displayedApps = installedApps.map { appIcon ->
                        val customLabel = customLabels[appIcon.packageName]
                        if (customLabel != null) {
                            appIcon.copy(label = customLabel)
                        } else {
                            appIcon
                        }
                    }

                    // ====== 新規追加：10個のインベントリスロット ======
                    // とりあえず最初は「空っぽ（null）」が10個並んだリストを作ります。
                    // 次のステップで、ここに好きなアプリをセットできるようにします。
                    var appSlots by remember { mutableStateOf<List<AppIcon?>>(List(10) { null }) }

                    HomeScreen(
                        appSlots = appSlots, // ← 新しいHomeScreenに10個の枠を渡す
                        appIcons = displayedApps,
                        onSlotClick = { slotIndex ->
                            // ====== 新規追加：スロットがタップされた時の処理 ======
                            // （今は空っぽのままにしておき、エラーが出ないことだけを確認します）
                            // TODO: 「枠が空ならアプリ選択画面を出す」「アプリが入っていれば起動する」処理を後で作ります。
                        },
                        onLongClickIcon = { longPressedIcon ->
                            editingIcon = longPressedIcon
                        }
                    )

                    // 編集中のアイコンがあれば、ダイアログを表示する
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