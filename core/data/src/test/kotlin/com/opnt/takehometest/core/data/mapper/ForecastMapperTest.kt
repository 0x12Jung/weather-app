package com.opnt.takehometest.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.WeatherCondition
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test

class ForecastMapperTest {

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
            time = listOf(
                "2026-04-16T12:00", "2026-04-16T13:00", "2026-04-16T14:00",
                "2026-04-16T15:00", "2026-04-16T16:00",
            ),
            temperature2m = listOf(21.0, 21.5, 22.0, 22.5, 23.0),
            weatherCode = listOf(0, 1, 2, 3, 61),
        ),
        daily = ForecastResponseDto.DailyDto(
            time = listOf("2026-04-16", "2026-04-17"),
            weatherCode = listOf(2, 61),
            temperatureMax = listOf(25.1, 22.0),
            temperatureMin = listOf(18.2, 17.5),
        ),
    )

    private val fetchedAt = Instant.parse("2026-04-16T14:00:00Z") // 14:00 UTC = 22:00 Taipei

    @Test
    fun `current maps temperature, condition, wind and isDay`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.current.temperatureCelsius).isEqualTo(22.5)
        assertThat(result.current.condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.current.windSpeedKmh).isEqualTo(3.4)
        assertThat(result.current.isDay).isTrue()
    }

    @Test
    fun `hourly entries before fetchedAt are filtered out`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.hourly).isEmpty()
    }

    @Test
    fun `hourly take 24 when future entries exceed 24`() {
        val earlyFetchedAt = Instant.parse("2026-04-16T00:00:00Z")
        val bigDto = dto.copy(
            hourly = ForecastResponseDto.HourlyDto(
                time = (0..30).map { "2026-04-17T%02d:00".format(it.coerceAtMost(23)) },
                temperature2m = List(31) { 20.0 },
                weatherCode = List(31) { 0 },
            )
        )
        val result = mapper.toDomain(bigDto, earlyFetchedAt)
        assertThat(result.hourly).hasSize(24)
    }

    @Test
    fun `daily maps date, condition, and min-max`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.daily).hasSize(2)
        assertThat(result.daily[0].date).isEqualTo(LocalDate(2026, 4, 16))
        assertThat(result.daily[0].condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.daily[0].maxTemperatureCelsius).isEqualTo(25.1)
        assertThat(result.daily[0].minTemperatureCelsius).isEqualTo(18.2)
        assertThat(result.daily[1].condition).isEqualTo(WeatherCondition.Rain)
    }

    @Test
    fun `fetchedAt is stamped on the result`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.fetchedAt).isEqualTo(fetchedAt)
    }
}
