package com.opnt.takehometest.feature.weather.weather

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object NoCity : WeatherUiState
    data class Success(
        val content: WeatherContentUiModel,
    ) : WeatherUiState
    data class Error(val error: WeatherError) : WeatherUiState
}

enum class WeatherError { NoInternet, Generic }
