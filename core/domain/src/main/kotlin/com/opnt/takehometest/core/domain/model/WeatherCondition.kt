package com.opnt.takehometest.core.domain.model

sealed interface WeatherCondition {
    data object Clear : WeatherCondition
    data object PartlyCloudy : WeatherCondition
    data object Cloudy : WeatherCondition
    data object Fog : WeatherCondition
    data object Drizzle : WeatherCondition
    data object Rain : WeatherCondition
    data object Snow : WeatherCondition
    data object Thunderstorm : WeatherCondition
    data class Unknown(val wmoCode: Int) : WeatherCondition

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> Clear
            2 -> PartlyCloudy
            3 -> Cloudy
            45, 48 -> Fog
            51, 53, 55, 56, 57 -> Drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> Rain
            71, 73, 75, 77, 85, 86 -> Snow
            95, 96, 99 -> Thunderstorm
            else -> Unknown(code)
        }
    }
}
