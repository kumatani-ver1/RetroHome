package com.retro.retrohome

import android.content.Context

class DockAppShortcutPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("dock_app_shortcut_prefs", Context.MODE_PRIVATE)

    fun savePackageName(packageName: String?) {
        prefs.edit().putString("package_name", packageName).apply()
    }

    fun getPackageName(): String? {
        return prefs.getString("package_name", null)
    }
}