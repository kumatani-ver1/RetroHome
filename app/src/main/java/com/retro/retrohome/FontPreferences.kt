package com.retro.retrohome

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 4つのフォントを定義
enum class AppFont(val label: String, val fontResId: Int) {
    FONT_1("フォント 1", R.font.f_1),
    FONT_2("フォント 2", R.font.f_2),
    FONT_3("フォント 3", R.font.f_3),
    FONT_4("フォント 4", R.font.f_4);

    val fontFamily: FontFamily
        get() = FontFamily(Font(fontResId))
}

// フォントの設定状態を保存・管理するクラス
class FontPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("font_prefs", Context.MODE_PRIVATE)

    private val _currentFont = MutableStateFlow(loadFont())
    val currentFont: StateFlow<AppFont> = _currentFont.asStateFlow()

    private fun loadFont(): AppFont {
        val fontName = prefs.getString("selected_font", AppFont.FONT_1.name)
        return try {
            AppFont.valueOf(fontName ?: AppFont.FONT_1.name)
        } catch (e: Exception) {
            AppFont.FONT_1
        }
    }

    fun saveFont(font: AppFont) {
        prefs.edit().putString("selected_font", font.name).apply()
        _currentFont.value = font
    }
}