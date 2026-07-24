package com.retro.retrohome

import android.content.Context
import android.content.SharedPreferences

/**
 * アプリごとのカスタムラベルとカスタムアイコンの URI を SharedPreferences に保存・管理するクラス
 */
class AppCustomPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_custom_prefs", Context.MODE_PRIVATE)

    // カスタムラベルの保存
    fun saveLabel(packageName: String, label: String) {
        prefs.edit().putString("label_$packageName", label).apply()
    }

    // カスタムラベルの取得（未設定の場合はデフォルトのラベルを返す）
    fun getLabel(packageName: String, defaultLabel: String): String {
        return prefs.getString("label_$packageName", defaultLabel) ?: defaultLabel
    }

    // カスタムアイコン URI の保存
    fun saveIconUri(packageName: String, iconUri: String?) {
        prefs.edit().putString("icon_uri_$packageName", iconUri).apply()
    }

    // カスタムアイコン URI の取得
    fun getIconUri(packageName: String): String? {
        return prefs.getString("icon_uri_$packageName", null)
    }
}