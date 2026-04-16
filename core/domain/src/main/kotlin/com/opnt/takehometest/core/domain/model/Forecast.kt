package com.opnt.takehometest.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Forecast(
    val fetchedAt: Instant,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
)

data class CurrentWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val windSpeedKmh: Double,
    val isDay: Boolean,
)

data class HourlyWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
)

data class DailyWeather(
    val date: LocalDate,
    val condition: WeatherCondition,
    val maxTemperatureCelsius: Double,
    val minTemperatureCelsius: Double,
)
