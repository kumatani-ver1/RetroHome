package com.retro.retrohome.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 変更されたアプリの名前（ラベル）を保存・読み込みするクラス
 */
object LabelPreferences {
    private const val PREFS_NAME = "retro_home_labels"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * カスタムラベルを保存する
     * @param packageName アプリのパッケージ名（キー）
     * @param label 変更後の新しい名前
     */
    fun saveLabel(context: Context, packageName: String, label: String) {
        getPrefs(context).edit().putString(packageName, label).apply()
    }

    /**
     * 保存されているすべてのカスタムラベルを読み込む
     * @return Map<パッケージ名, カスタムラベル>
     */
    fun loadLabels(context: Context): Map<String, String> {
        val prefs = getPrefs(context)
        val labels = mutableMapOf<String, String>()

        // 保存されているすべてのデータを取り出してMapに詰める
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                labels[key] = value
            }
        }
        return labels
    }
}