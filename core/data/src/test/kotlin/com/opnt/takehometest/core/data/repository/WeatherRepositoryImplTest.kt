package com.opnt.takehometest.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.mapper.ForecastMapper
import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.WeatherCondition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Test

class WeatherRepositoryImplTest {

    private val api: OpenMeteoForecastApi = mockk()
    private val mapper = ForecastMapper()

    private val dto = ForecastResponseDto(
        latitude = 25.03,
        longitude = 121.57,
        timezone = "Asia/Taipei",
        current = ForecastResponseDto.CurrentDto(
            time = "2026-04-16T14:00",
            temperature2m = 22.5,
            weatherCode = 2,
            windSpeed10m = 3.4,
            isDay = 1,
        ),
        hourly = ForecastResponseDto.HourlyDto(
            time = listOf("2026-04-16T14:00"),
            temperature2m = listOf(22.5),
            weatherCode = listOf(2),
        ),
        daily = ForecastResponseDto.DailyDto(
            time = listOf("2026-04-16"),
            weatherCode = listOf(2),
            temperatureMax = listOf(25.1),
            temperatureMin = listOf(18.2),
        ),
    )

    @Test
    fun `getForecast calls API with lat, lon, timezone and maps response`() = runTest {
        coEvery {
            api.getForecast(lat = 25.0330, lon = 121.5654, timezone = "Asia/Taipei")
        } returns dto
        val fixedClock = object : Clock {
            override fun now(): Instant = Instant.parse("2026-04-16T14:00:00Z")
        }
        val repo = WeatherRepositoryImpl(api, mapper, fixedClock)

        val result = repo.getForecast(Coordinates(25.0330, 121.5654), "Asia/Taipei")

        assertThat(result.current.condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.current.temperatureCelsius).isEqualTo(22.5)
        coVerify(exactly = 1) { api.getForecast(lat = 25.0330, lon = 121.5654, timezone = "Asia/Taipei") }
    }
}
