package com.opnt.takehometest.feature.weather.weather

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.domain.usecase.GetForecastUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test

class WeatherViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val observeSelectedCity: ObserveSelectedCityUseCase = mockk()
    private val getForecast: GetForecastUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val forecast = Forecast(
        fetchedAt = Instant.parse("2026-04-16T14:00:00Z"),
        current = CurrentWeather(
            time = Instant.parse("2026-04-16T14:00:00Z"),
            temperatureCelsius = 22.5,
            condition = WeatherCondition.PartlyCloudy,
            windSpeedKmh = 3.4,
            isDay = true,
        ),
        hourly = emptyList(),
        daily = emptyList(),
    )

    @Test
    fun `emits NoCity when selected city is null`() = runTest {
        every { observeSelectedCity() } returns flowOf(null)
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            assertThat(awaitItem()).isEqualTo(WeatherUiState.NoCity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Success when city yields a forecast`() = runTest {
        every { observeSelectedCity() } returns flowOf(taipei)
        coEvery { getForecast(taipei) } returns forecast
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(WeatherUiState.Success::class.java)
            assertThat((success as WeatherUiState.Success).forecast).isEqualTo(forecast)
            assertThat(success.city).isEqualTo(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when getForecast throws`() = runTest {
        every { observeSelectedCity() } returns flowOf(taipei)
        coEvery { getForecast(taipei) } throws java.io.IOException("no network")
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            val err = awaitItem() as WeatherUiState.Error
            assertThat(err.error).isEqualTo(WeatherError.NoInternet)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRetry refetches forecast for current city`() = runTest {
        val cityFlow = MutableStateFlow<City?>(taipei)
        every { observeSelectedCity() } returns cityFlow
        coEvery { getForecast(taipei) } throws java.io.IOException("no network") andThen forecast

        val vm = WeatherViewModel(observeSelectedCity, getForecast)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            assertThat(awaitItem()).isInstanceOf(WeatherUiState.Error::class.java)

            vm.onRetry()
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            assertThat(awaitItem()).isInstanceOf(WeatherUiState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WeatherViewModel does not expose onRefresh`() {
        val publicMethodNames = WeatherViewModel::class.java.methods.map { it.name }

        assertThat(publicMethodNames).doesNotContain("onRefresh")
    }

    @Test
    fun `WeatherUiState Success does not keep isRefreshing field`() {
        val fieldNames = WeatherUiState.Success::class.java.declaredFields.map { it.name }

        assertThat(fieldNames).doesNotContain("isRefreshing")
    }
}
