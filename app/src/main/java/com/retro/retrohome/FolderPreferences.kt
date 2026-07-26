package com.retro.retrohome

import android.content.Context
import org.json.JSONObject

/**
 * アプリドロワー内の「フォルダ」（フォルダ名→中に入れたアプリのpackageName一覧）を保存・読込するクラス。
 */
class FolderPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("folder_prefs", Context.MODE_PRIVATE)

    // フォルダ名 → その中に入っているpackageNameのリスト
    fun getFolders(): Map<String, List<String>> {
        val jsonString = prefs.getString("folders", null) ?: return emptyMap()
        return try {
            val json = JSONObject(jsonString)
            val result = mutableMapOf<String, List<String>>()
            json.keys().forEach { folderName ->
                val array = json.getJSONArray(folderName)
                val packages = (0 until array.length()).map { array.getString(it) }
                result[folderName] = packages
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun saveFolders(folders: Map<String, List<String>>) {
        val json = JSONObject()
        folders.forEach { (folderName, packages) ->
            json.put(folderName, org.json.JSONArray(packages))
        }
        prefs.edit().putString("folders", json.toString()).apply()
    }

    // 指定したアプリが、今どのフォルダに入っているか（無ければnull）
    fun getFolderForPackage(packageName: String): String? {
        return getFolders().entries.find { packageName in it.value }?.key
    }

    // アプリを指定フォルダに入れる（既に別フォルダに入っていればそこからは自動で外す）
    fun addAppToFolder(folderName: String, packageName: String) {
        val folders = getFolders().mapValues { it.value.toMutableList() }.toMutableMap()
        folders.values.forEach { it.remove(packageName) }
        val target = folders.getOrPut(folderName) { mutableListOf() }
        if (packageName !in target) target.add(packageName)
        saveFolders(folders)
    }

    // アプリをフォルダから出す（未分類に戻す）
    fun removeAppFromFolder(packageName: String) {
        val folders = getFolders().mapValues { it.value.toMutableList() }.toMutableMap()
        folders.values.forEach { it.remove(packageName) }
        saveFolders(folders)
    }
}