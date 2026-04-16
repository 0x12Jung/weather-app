package com.opnt.takehometest.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
) {
    @Serializable
    data class CurrentDto(
        val time: String,
        @SerialName("temperature_2m") val temperature2m: Double,
        @SerialName("weather_code") val weatherCode: Int,
        @SerialName("wind_speed_10m") val windSpeed10m: Double,
        @SerialName("is_day") val isDay: Int,
    )

    @Serializable
    data class HourlyDto(
        val time: List<String>,
        @SerialName("temperature_2m") val temperature2m: List<Double>,
        @SerialName("weather_code") val weatherCode: List<Int>,
    )

    @Serializable
    data class DailyDto(
        val time: List<String>,
        @SerialName("weather_code") val weatherCode: List<Int>,
        @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
        @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    )
}
