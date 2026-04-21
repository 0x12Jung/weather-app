package com.opnt.takehometest.feature.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.usecase.GetForecastUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
) : ViewModel() {

    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<WeatherUiState> = observeSelectedCity()
        .flatMapLatest { city ->
            if (city == null) flowOf(WeatherUiState.NoCity)
            else refresh.onStart { emit(Unit) }.flatMapLatest { fetchFor(city) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeatherUiState.Loading)

    fun onRetry() { refresh.tryEmit(Unit) }

    private fun fetchFor(city: City): Flow<WeatherUiState> = flow {
        emit(WeatherUiState.Loading)
        emit(WeatherUiState.Success(getForecast(city).toContentUiModel(city)))
    }.catch { emit(WeatherUiState.Error(it.toError())) }
}

internal fun Throwable.toError(): WeatherError = when (this) {
    is IOException -> WeatherError.NoInternet
    else -> WeatherError.Generic
}

private fun Forecast.toContentUiModel(city: City): WeatherContentUiModel {
    val zone = TimeZone.of(city.timezone)
    return WeatherContentUiModel(
        cityTitle = "${city.name}, ${city.country}",
        current = CurrentWeatherUiModel(
            temperatureText = "${current.temperatureCelsius.toInt()}°C",
            condition = current.condition,
            windSpeedKmh = current.windSpeedKmh,
        ),
        hourly = hourly.map { hour ->
            HourlyWeatherUiModel(
                epochMillis = hour.time.toEpochMilliseconds(),
                hourText = formatHourlyHour(hour.time, zone),
                temperatureText = "${hour.temperatureCelsius.toInt()}°",
                condition = hour.condition,
            )
        },
        daily = daily.map { day ->
            DailyWeatherUiModel(
                epochDays = day.date.toEpochDays(),
                dayText = day.date.dayOfWeek.name.take(3),
                condition = day.condition,
                temperatureRangeText = "${day.minTemperatureCelsius.toInt()}° / ${day.maxTemperatureCelsius.toInt()}°",
            )
        },
    )
}
