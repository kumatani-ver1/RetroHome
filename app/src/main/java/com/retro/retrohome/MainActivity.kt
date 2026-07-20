package com.retro.retrohome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.retro.retrohome.model.AppIcon
import com.retro.retrohome.ui.component.IconGrid
import com.retro.retrohome.ui.theme.RetroHomeTheme

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
                    // STEP1: 動作確認用の仮アイコン20個（4列×5行）
                    val dummyIcons = List(20) { index ->
                        AppIcon(
                            label = "アプリ${index + 1}",
                            packageName = "dummy.package.$index"
                        )
                    }
                    IconGrid(appIcons = dummyIcons)
                }
            }
        }
    }
}