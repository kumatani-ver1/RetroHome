package com.retro.retrohome.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 変更されたアプリのアイコンUriを保存・読み込みするクラス
 */
object IconPreferences {
    private const val PREFS_NAME = "retro_home_icons"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * カスタムアイコンのUriを保存する
     */
    fun saveIconUri(context: Context, packageName: String, uriString: String?) {
        val editor = getPrefs(context).edit()
        if (uriString != null) {
            editor.putString(packageName, uriString)
        } else {
            editor.remove(packageName)
        }
        editor.apply()
    }

    /**
     * 保存されているすべてのカスタムアイコンのUriを読み込む
     */
    fun loadIconUris(context: Context): Map<String, String> {
        val prefs = getPrefs(context)
        val uris = mutableMapOf<String, String>()

        prefs.all.forEach { (key, value) ->
            if (value is String) {
                uris[key] = value
            }
        }
        return uris
    }
}