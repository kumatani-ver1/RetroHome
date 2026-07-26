package com.retro.retrohome.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.retrohome.AppFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

data class DailyWeather(
    val dateLabel: String,
    val weatherCode: Int,
    val amRainProbability: Int,
    val pmRainProbability: Int
)

private val WEEKDAY_LABELS = arrayOf("日", "月", "火", "水", "木", "金", "土")

private fun roundToNearestTen(value: Int): Int {
    return ((value + 5) / 10) * 10
}

private fun iconForWeatherCode(code: Int): ImageVector {
    return when (code) {
        0, 1 -> Icons.Outlined.WbSunny
        2 -> Icons.Outlined.WbCloudy
        3, 45, 48 -> Icons.Outlined.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Outlined.Grain
        71, 73, 75, 77, 85, 86 -> Icons.Outlined.AcUnit
        95, 96, 99 -> Icons.Outlined.Thunderstorm
        else -> Icons.Outlined.WbCloudy
    }
}

private fun fetchWeather(latitude: Double, longitude: Double): List<DailyWeather> {
    val url = URL(
        "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&daily=weathercode&hourly=precipitation_probability" +
                "&timezone=Asia%2FTokyo&forecast_days=4"
    )
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 5000
    connection.readTimeout = 5000
    val response = connection.inputStream.bufferedReader().use { it.readText() }
    val json = JSONObject(response)

    val daily = json.getJSONObject("daily")
    val dailyTimes = daily.getJSONArray("time")
    val dailyCodes = daily.getJSONArray("weathercode")

    val hourly = json.getJSONObject("hourly")
    val hourlyTimes = hourly.getJSONArray("time")
    val hourlyProbabilities = hourly.getJSONArray("precipitation_probability")

    val result = mutableListOf<DailyWeather>()
    for (dayIndex in 0 until dailyTimes.length()) {
        val dateStr = dailyTimes.getString(dayIndex)
        val parts = dateStr.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val weekday = WEEKDAY_LABELS[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val dateLabel = "$month/$day($weekday)"

        var amMax = 0
        var pmMax = 0
        for (hourIndex in 0 until hourlyTimes.length()) {
            val hourStr = hourlyTimes.getString(hourIndex)
            if (!hourStr.startsWith(dateStr)) continue
            val hour = hourStr.substring(11, 13).toInt()
            val probability = hourlyProbabilities.optInt(hourIndex, 0)
            if (hour < 12) {
                if (probability > amMax) amMax = probability
            } else {
                if (probability > pmMax) pmMax = probability
            }
        }

        result.add(
            DailyWeather(
                dateLabel = dateLabel,
                weatherCode = dailyCodes.getInt(dayIndex),
                amRainProbability = roundToNearestTen(amMax),
                pmRainProbability = roundToNearestTen(pmMax)
            )
        )
    }
    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherWidget(
    latitude: Double?,
    longitude: Double?,
    onRequestLocationPicker: () -> Unit,
    currentFont: AppFont? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dailyWeatherList by remember { mutableStateOf<List<DailyWeather>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorOccurred by remember { mutableStateOf(false) }
    val fontFamily = currentFont?.fontFamily ?: FontFamily.Default

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            isLoading = true
            errorOccurred = false
            try {
                dailyWeatherList = withContext(Dispatchers.IO) { fetchWeather(latitude, longitude) }
            } catch (e: Exception) {
                e.printStackTrace()
                errorOccurred = true
            }
            isLoading = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = {
                    if (latitude == null) {
                        onRequestLocationPicker()
                    } else {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://weather.yahoo.co.jp/weather/jp/38/7310/38201.html")
                        )
                        context.startActivity(intent)
                    }
                },
                onLongClick = onRequestLocationPicker
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        when {
            latitude == null -> {
                Text(
                    text = "タップして地域を設定",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(24.dp)
                )
            }
            isLoading -> {
                Text(
                    text = "天気を取得中…",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(24.dp)
                )
            }
            errorOccurred -> {
                Text(
                    text = "天気の取得に失敗しました",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(24.dp)
                )
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    dailyWeatherList.forEach { day ->
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = day.dateLabel, color = Color.White, fontSize = 11.sp, fontFamily = fontFamily)
                                Icon(
                                    imageVector = iconForWeatherCode(day.weatherCode),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(18.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "午前${day.amRainProbability}%", color = Color.White, fontSize = 10.sp, fontFamily = fontFamily)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "午後${day.pmRainProbability}%", color = Color.White, fontSize = 10.sp, fontFamily = fontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}