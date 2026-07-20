package com.retro.retrohome.model

/**
 * ホーム画面に表示する1個分のアイコン情報
 *
 * @param label 画面に表示するアプリの名前（ラベル）
 * @param packageName このアプリを起動するための識別子（例: "com.android.chrome"）
 */
data class AppIcon(
    val label: String,
    val packageName: String
)