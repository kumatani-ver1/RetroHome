package com.retro.retrohome

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// バックアップ対象の設定ファイル一覧（今後、新しい設定ファイルを増やしたらここにも追加する）
private val PREF_FILE_NAMES = listOf(
    "app_custom_prefs",
    "slot_prefs",
    "font_prefs",
    "nav_button_prefs",
    "photo_widget_prefs",
    "weather_prefs",
    "message_widget_prefs",
    "dock_app_shortcut_prefs"
)

/**
 * アプリ内の全設定（フォント・アイコン配置・各ウィジェットの設定など）を
 * 1つのJSONにまとめてエクスポート／インポートする仕組み。
 */
object SettingsBackupManager {

    fun exportToJson(context: Context): String {
        val root = JSONObject()
        for (fileName in PREF_FILE_NAMES) {
            val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            val fileJson = JSONObject()
            for ((key, value) in prefs.all) {
                when (value) {
                    is String -> fileJson.put(key, value)
                    is Int -> fileJson.put(key, value)
                    is Float -> fileJson.put(key, value.toDouble())
                    is Boolean -> fileJson.put(key, value)
                    is Long -> fileJson.put(key, value)
                    is Set<*> -> {
                        val array = JSONArray()
                        value.forEach { array.put(it.toString()) }
                        fileJson.put(key, array)
                    }
                }
            }
            root.put(fileName, fileJson)
        }
        return root.toString()
    }

    fun importFromJson(context: Context, jsonString: String) {
        val root = JSONObject(jsonString)
        for (fileName in PREF_FILE_NAMES) {
            if (!root.has(fileName)) continue
            val fileJson = root.getJSONObject(fileName)
            val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            val keys = fileJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                when (val value = fileJson.get(key)) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Boolean -> editor.putBoolean(key, value)
                    is Long -> editor.putLong(key, value)
                    is JSONArray -> {
                        val set = mutableSetOf<String>()
                        for (i in 0 until value.length()) {
                            set.add(value.getString(i))
                        }
                        editor.putStringSet(key, set)
                    }
                }
            }
            editor.apply()
        }
    }
}