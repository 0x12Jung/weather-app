package com.opnt.takehometest.feature.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
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
        emit(WeatherUiState.Success(city, getForecast(city)))
    }.catch { emit(WeatherUiState.Error(it.toError())) }
}

internal fun Throwable.toError(): WeatherError = when (this) {
    is IOException -> WeatherError.NoInternet
    else -> WeatherError.Generic
}
