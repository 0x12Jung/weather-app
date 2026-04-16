package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class AddCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(city: City) = repository.addCity(city)
}
