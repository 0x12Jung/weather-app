package com.opnt.takehometest.core.data.repository

import com.opnt.takehometest.core.data.mapper.ForecastMapper
import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.datetime.Clock

internal class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoForecastApi,
    private val mapper: ForecastMapper,
    private val clock: Clock,
) : WeatherRepository {

    override suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast {
        val dto = api.getForecast(
            lat = coordinates.latitude,
            lon = coordinates.longitude,
            timezone = timezone,
        )
        return mapper.toDomain(dto, clock.now())
    }
}
