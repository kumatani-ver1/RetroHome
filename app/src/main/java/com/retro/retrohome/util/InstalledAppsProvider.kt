package com.retro.retrohome.util

import android.content.Context
import android.content.Intent
import com.retro.retrohome.model.AppIcon

/**
 * 端末にインストールされている「起動可能なアプリ」の一覧を取得する
 */
object InstalledAppsProvider {

    fun getInstalledApps(context: Context): List<AppIcon> {
        val packageManager = context.packageManager

        // 「ホーム画面から起動できるアプリ」を探すための問い合わせ内容
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // 条件に合うアプリを全部取得する
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

        return resolveInfoList.map { resolveInfo ->
            AppIcon(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.label } // 名前順に並べておく
    }
}