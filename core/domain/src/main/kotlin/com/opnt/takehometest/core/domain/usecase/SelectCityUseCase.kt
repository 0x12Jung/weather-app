package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class SelectCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) = repository.setSelectedCity(cityId)
}
