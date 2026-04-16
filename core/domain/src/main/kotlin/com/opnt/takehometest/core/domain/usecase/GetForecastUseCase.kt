package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository,
) {
    suspend operator fun invoke(city: City): Forecast =
        repository.getForecast(city.coordinates, city.timezone)
}
