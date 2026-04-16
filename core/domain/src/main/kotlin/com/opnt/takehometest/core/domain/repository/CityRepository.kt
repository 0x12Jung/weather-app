package com.opnt.takehometest.core.domain.repository

import com.opnt.takehometest.core.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    fun observeSavedCities(): Flow<List<City>>
    fun observeSelectedCity(): Flow<City?>
    suspend fun searchCities(query: String): List<City>
    suspend fun addCity(city: City)
    suspend fun removeCity(cityId: Long)
    suspend fun setSelectedCity(cityId: Long)
}
