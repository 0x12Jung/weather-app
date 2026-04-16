package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSelectedCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    operator fun invoke(): Flow<City?> = repository.observeSelectedCity()
}
