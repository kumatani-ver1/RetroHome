package com.retro.retrohome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
                    // STEP2.5: ダミーデータの代わりに、実際にインストールされているアプリ一覧を使う
                    val installedApps = InstalledAppsProvider.getInstalledApps(applicationContext)
                    HomeScreen(appIcons = installedApps)
                }
            }
        }
    }
}