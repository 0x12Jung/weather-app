package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class RemoveCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) = repository.removeCity(cityId)
}
