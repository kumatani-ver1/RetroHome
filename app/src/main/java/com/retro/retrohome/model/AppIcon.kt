package com.retro.retrohome.model

import android.graphics.drawable.Drawable

/**
 * ホーム画面に表示する1個分のアイコン情報
 *
 * @param label 画面に表示するアプリの名前（ラベル）
 * @param packageName このアプリを起動するための識別子（例: "com.android.chrome"）
 * @param icon アプリのアイコン画像（取得できなかった場合は null）
 * @param iconUri ユーザーがカスタム設定したアイコン画像のUri文字列（未設定の場合はnull）
 */
data class AppIcon(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    val iconUri: String? = null
)