package com.retro.retrohome.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeocodeResult(
    val name: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
)

// Open-Meteoの地域検索APIに問い合わせる（APIキー不要）
private suspend fun searchLocations(query: String): List<GeocodeResult> = withContext(Dispatchers.IO) {
    try {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=10&language=ja&format=json")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)
        val resultsArray = json.optJSONArray("results") ?: return@withContext emptyList()
        (0 until resultsArray.length()).map { i ->
            val item = resultsArray.getJSONObject(i)
            GeocodeResult(
                name = item.getString("name"),
                admin1 = if (item.has("admin1")) item.optString("admin1") else null,
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude")
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

/**
 * 地域名（市区町村）で検索し、天気予報を取得する地域を選ぶダイアログ
 */
@Composable
fun LocationSearchDialog(
    onLocationSelected: (name: String, latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeocodeResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("地域を検索") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("市区町村名（例：松山市）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (query.isNotBlank()) {
                            isSearching = true
                            scope.launch {
                                results = searchLocations(query)
                                isSearching = false
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("検索")
                }

                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }

                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(results) { result ->
                        Text(
                            text = "${result.name}（${result.admin1 ?: ""}）",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(result.name, result.latitude, result.longitude)
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}