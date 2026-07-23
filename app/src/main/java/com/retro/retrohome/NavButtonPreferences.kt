package com.retro.retrohome

import android.content.Context

class NavButtonPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("nav_button_prefs", Context.MODE_PRIVATE)

    fun saveLeftButtonIconUri(uriString: String?) {
        prefs.edit().putString("left_button_icon", uriString).apply()
    }

    fun getLeftButtonIconUri(): String? {
        return prefs.getString("left_button_icon", null)
    }

    fun saveRightButtonIconUri(uriString: String?) {
        prefs.edit().putString("right_button_icon", uriString).apply()
    }

    fun getRightButtonIconUri(): String? {
        return prefs.getString("right_button_icon", null)
    }
}