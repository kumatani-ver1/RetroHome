package com.retro.retrohome

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeatherLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * 天気予報を取得する「固定の地域」を保存・読込するクラス。
 */
class WeatherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val _location = MutableStateFlow(loadLocation())
    val location: StateFlow<WeatherLocation?> = _location.asStateFlow()

    private fun loadLocation(): WeatherLocation? {
        val name = prefs.getString("location_name", null) ?: return null
        val lat = prefs.getFloat("location_lat", 0f).toDouble()
        val lon = prefs.getFloat("location_lon", 0f).toDouble()
        return WeatherLocation(name, lat, lon)
    }

    fun saveLocation(location: WeatherLocation) {
        prefs.edit()
            .putString("location_name", location.name)
            .putFloat("location_lat", location.latitude.toFloat())
            .putFloat("location_lon", location.longitude.toFloat())
            .apply()
        _location.value = location
    }
}