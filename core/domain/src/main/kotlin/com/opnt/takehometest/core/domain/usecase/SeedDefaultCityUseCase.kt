package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SeedDefaultCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke() {
        val existing = repository.observeSavedCities().first()
        if (existing.isNotEmpty()) return
        repository.addCity(DEFAULT_CITY)
        repository.setSelectedCity(DEFAULT_CITY.id)
    }

    companion object {
        val DEFAULT_CITY = City(
            id = 1668341L,
            name = "Taipei",
            country = "Taiwan",
            admin = "Taipei City",
            coordinates = Coordinates(25.0330, 121.5654),
            timezone = "Asia/Taipei",
        )
    }
}
