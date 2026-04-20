package com.opnt.takehometest.feature.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.GetForecastUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentCity: City? = null

    init {
        viewModelScope.launch {
            observeSelectedCity().collectLatest { city ->
                currentCity = city
                if (city == null) {
                    _uiState.value = WeatherUiState.NoCity
                } else {
                    loadForecast(city)
                }
            }
        }
    }

    fun onRetry() {
        val city = currentCity ?: return
        viewModelScope.launch { loadForecast(city) }
    }

    fun onRefresh() {
        val city = currentCity ?: return
        viewModelScope.launch {
            (_uiState.value as? WeatherUiState.Success)?.let {
                _uiState.value = it.copy(isRefreshing = true)
            }
            try {
                val forecast = getForecast(city)
                _uiState.value = WeatherUiState.Success(city, forecast)
            } catch (t: Throwable) {
                _uiState.value = WeatherUiState.Error(t.toUserMessage())
            }
        }
    }

    private suspend fun loadForecast(city: City) {
        _uiState.value = WeatherUiState.Loading
        try {
            val forecast = getForecast(city)
            _uiState.value = WeatherUiState.Success(city, forecast)
        } catch (t: Throwable) {
            _uiState.value = WeatherUiState.Error(t.toUserMessage())
        }
    }
}

internal fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "No internet connection.\nCheck your network and try again."
    else -> "Something went wrong."
}
