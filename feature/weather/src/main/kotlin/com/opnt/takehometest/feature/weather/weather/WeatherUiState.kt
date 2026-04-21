package com.opnt.takehometest.feature.weather.weather

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Forecast

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object NoCity : WeatherUiState
    data class Success(
        val city: City,
        val forecast: Forecast,
    ) : WeatherUiState
    data class Error(val error: WeatherError) : WeatherUiState
}

enum class WeatherError { NoInternet, Generic }
