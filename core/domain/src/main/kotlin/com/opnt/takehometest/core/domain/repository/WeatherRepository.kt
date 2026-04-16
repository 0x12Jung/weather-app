package com.opnt.takehometest.core.domain.repository

import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.Forecast

interface WeatherRepository {
    suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast
}
