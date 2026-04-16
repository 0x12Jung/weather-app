package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test

class GetForecastUseCaseTest {

    private val repo: WeatherRepository = mockk()
    private val useCase = GetForecastUseCase(repo)

    @Test
    fun `invoke delegates to repository with city coordinates and timezone`() = runTest {
        val city = City(
            id = 1L,
            name = "Taipei",
            country = "Taiwan",
            admin = "Taipei City",
            coordinates = Coordinates(25.0330, 121.5654),
            timezone = "Asia/Taipei",
        )
        val expected = Forecast(
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
        coEvery { repo.getForecast(city.coordinates, city.timezone) } returns expected

        val actual = useCase(city)

        assertThat(actual).isEqualTo(expected)
        coVerify(exactly = 1) { repo.getForecast(city.coordinates, city.timezone) }
    }
}
