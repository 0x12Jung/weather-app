package com.opnt.takehometest.core.data.mapper

import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.DailyWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.HourlyWeather
import com.opnt.takehometest.core.domain.model.WeatherCondition
import javax.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class ForecastMapper @Inject constructor() {

    fun toDomain(dto: ForecastResponseDto, fetchedAt: Instant): Forecast {
        val zone = TimeZone.of(dto.timezone)
        return Forecast(
            fetchedAt = fetchedAt,
            current = dto.current.toDomain(zone),
            hourly = mapHourly(dto.hourly, zone, fetchedAt),
            daily = mapDaily(dto.daily),
        )
    }

    private fun ForecastResponseDto.CurrentDto.toDomain(zone: TimeZone) =
        CurrentWeather(
            time = LocalDateTime.parse(time).toInstant(zone),
            temperatureCelsius = temperature2m,
            condition = WeatherCondition.fromWmoCode(weatherCode),
            windSpeedKmh = windSpeed10m,
            isDay = isDay == 1,
        )

    private fun mapHourly(
        dto: ForecastResponseDto.HourlyDto,
        zone: TimeZone,
        fetchedAt: Instant,
    ): List<HourlyWeather> =
        dto.time.indices.map { i ->
            HourlyWeather(
                time = LocalDateTime.parse(dto.time[i]).toInstant(zone),
                temperatureCelsius = dto.temperature2m[i],
                condition = WeatherCondition.fromWmoCode(dto.weatherCode[i]),
            )
        }.filter { it.time >= fetchedAt }.take(HOURLY_LIMIT)

    private fun mapDaily(dto: ForecastResponseDto.DailyDto): List<DailyWeather> =
        dto.time.indices.map { i ->
            DailyWeather(
                date = LocalDate.parse(dto.time[i]),
                condition = WeatherCondition.fromWmoCode(dto.weatherCode[i]),
                maxTemperatureCelsius = dto.temperatureMax[i],
                minTemperatureCelsius = dto.temperatureMin[i],
            )
        }

    companion object {
        const val HOURLY_LIMIT = 24
    }
}
