package com.retro.retrohome

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * res/font フォルダ内の f_数字.ttf(または.otf) を自動検出してフォント一覧を作る。
 * 新しいフォントを追加したいときは res/font に f_11.ttf のようなファイルを置いて
 * ビルドし直すだけでよく、このファイルを編集する必要はない。
 */
data class AppFont(
    val resourceName: String, // 例："f_1"（保存用のキーとしても使う）
    val label: String,
    val fontResId: Int
) {
    val fontFamily: FontFamily
        get() = FontFamily(Font(fontResId))

    companion object {
        private val NUMBER_REGEX = Regex("""f_(\d+)""")

        // R.font クラスのフィールドを反射で読み取り、f_数字 の命名規則のものだけを番号順に並べる
        val entries: List<AppFont> by lazy {
            R.font::class.java.fields
                .mapNotNull { field ->
                    val match = NUMBER_REGEX.matchEntire(field.name) ?: return@mapNotNull null
                    val number = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                    Triple(field.name, number, field.getInt(null))
                }
                .sortedBy { it.second } // f_1, f_2, ... f_10, f_11 と数値順にする
                .map { (name, number, resId) ->
                    AppFont(resourceName = name, label = "フォント $number", fontResId = resId)
                }
        }

        val default: AppFont
            get() = entries.first()

        fun byResourceName(name: String?): AppFont =
            entries.find { it.resourceName == name } ?: default
    }
}

// フォントの設定状態を保存・管理するクラス
class FontPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("font_prefs", Context.MODE_PRIVATE)

    private val _currentFont = MutableStateFlow(loadFont())
    val currentFont: StateFlow<AppFont> = _currentFont.asStateFlow()

    private fun loadFont(): AppFont {
        val savedName = prefs.getString("selected_font", null)
        return AppFont.byResourceName(savedName)
    }

    fun saveFont(font: AppFont) {
        prefs.edit().putString("selected_font", font.resourceName).apply()
        _currentFont.value = font
    }
}