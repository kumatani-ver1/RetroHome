package com.retro.retrohome.util

import android.content.Context
import android.content.SharedPreferences

/**
 * ホーム画面の10個のスロット（枠）にセットされたアプリを保存・読み込みするクラス
 */
object SlotPreferences {
    private const val PREFS_NAME = "retro_home_slots"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * スロットの変更を保存する
     * @param slotIndex 枠の番号（0〜9）
     * @param packageName 保存するアプリのパッケージ名（nullの場合は空枠として保存）
     */
    fun saveSlot(context: Context, slotIndex: Int, packageName: String?) {
        val editor = getPrefs(context).edit()
        if (packageName == null) {
            // (なし)が選ばれたら保存データを削除
            editor.remove("slot_$slotIndex")
        } else {
            // アプリが選ばれたらパッケージ名を保存
            editor.putString("slot_$slotIndex", packageName)
        }
        editor.apply()
    }

    /**
     * 保存されているスロット情報をすべて読み込む
     * @return Map<枠の番号, パッケージ名>
     */
    fun loadSlots(context: Context): Map<Int, String> {
        val prefs = getPrefs(context)
        val slots = mutableMapOf<Int, String>()

        for (i in 0 until 10) {
            val packageName = prefs.getString("slot_$i", null)
            if (packageName != null) {
                slots[i] = packageName
            }
        }
        return slots
    }
}