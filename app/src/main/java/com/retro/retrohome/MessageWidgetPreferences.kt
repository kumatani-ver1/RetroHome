package com.retro.retrohome

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * メッセージウィジェットで使う「.txtが入ったフォルダ」と「表示間隔」を保存・読込するクラス。
 */
class MessageWidgetPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("message_widget_prefs", Context.MODE_PRIVATE)

    private val _folderUri = MutableStateFlow(loadFolderUri())
    val folderUri: StateFlow<Uri?> = _folderUri.asStateFlow()

    private val _intervalSeconds = MutableStateFlow(loadIntervalSeconds())
    val intervalSeconds: StateFlow<Int> = _intervalSeconds.asStateFlow()

    private fun loadFolderUri(): Uri? {
        val saved = prefs.getString("folder_uri", null) ?: return null
        return Uri.parse(saved)
    }

    fun saveFolderUri(uri: Uri) {
        prefs.edit().putString("folder_uri", uri.toString()).apply()
        _folderUri.value = uri
    }

    // デフォルトは30秒
    private fun loadIntervalSeconds(): Int {
        return prefs.getInt("interval_seconds", 30)
    }

    fun saveIntervalSeconds(seconds: Int) {
        prefs.edit().putInt("interval_seconds", seconds).apply()
        _intervalSeconds.value = seconds
    }
}