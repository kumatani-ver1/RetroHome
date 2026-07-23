package com.retro.retrohome

import android.content.Context
import com.retro.retrohome.model.AppIcon
import org.json.JSONArray
import org.json.JSONObject

class SlotPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("slot_prefs", Context.MODE_PRIVATE)

    /**
     * スロットの一覧を保存する
     * AppIcon が null の場合は空オブジェクトとして記録
     */
    fun saveSlots(slots: List<AppIcon?>) {
        val jsonArray = JSONArray()
        slots.forEach { app ->
            val jsonObject = JSONObject()
            if (app != null) {
                jsonObject.put("packageName", app.packageName)
                jsonObject.put("label", app.label)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString("saved_slots", jsonArray.toString()).apply()
    }

    /**
     * 保存されているスロット情報を読み出す
     * 端末にインストールされているアプリ一覧（installedApps）と突き合わせて AppIcon を復元する
     */
    fun loadSlots(totalSlotCount: Int, installedApps: List<AppIcon>): List<AppIcon?> {
        val jsonString = prefs.getString("saved_slots", null) ?: return List(totalSlotCount) { null }
        val result = MutableList<AppIcon?>(totalSlotCount) { null }

        try {
            val jsonArray = JSONArray(jsonString)
            val appMap = installedApps.associateBy { it.packageName }

            for (i in 0 until minOf(jsonArray.length(), totalSlotCount)) {
                val jsonObject = jsonArray.optJSONObject(i) ?: continue
                val packageName = jsonObject.optString("packageName", "")
                val savedLabel = jsonObject.optString("label", "")

                if (packageName.isNotEmpty()) {
                    val originalApp = appMap[packageName]
                    if (originalApp != null) {
                        // アイコン画像は PackageManager から復元した originalApp のものを使い、
                        // ラベルは保存されているカスタムラベル（変更後）を適用
                        val labelToUse = if (savedLabel.isNotEmpty()) savedLabel else originalApp.label
                        result[i] = originalApp.copy(label = labelToUse)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}