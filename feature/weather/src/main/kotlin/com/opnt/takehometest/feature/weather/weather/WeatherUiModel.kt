package com.opnt.takehometest.feature.weather.weather

import com.opnt.takehometest.core.domain.model.WeatherCondition

data class WeatherContentUiModel(
    val cityTitle: String,
    val current: CurrentWeatherUiModel,
    val hourly: List<HourlyWeatherUiModel>,
    val daily: List<DailyWeatherUiModel>,
)

data class CurrentWeatherUiModel(
    val temperatureText: String,
    val condition: WeatherCondition,
    val windSpeedKmh: Double,
)

data class HourlyWeatherUiModel(
    val epochMillis: Long,
    val hourText: String,
    val temperatureText: String,
    val condition: WeatherCondition,
)

data class DailyWeatherUiModel(
    val epochDays: Int,
    val dayText: String,
    val condition: WeatherCondition,
    val temperatureRangeText: String,
)
