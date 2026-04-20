package com.opnt.takehometest.core.data.repository

import androidx.datastore.core.DataStore
import com.opnt.takehometest.core.data.local.SavedCitiesState
import com.opnt.takehometest.core.data.mapper.toCity
import com.opnt.takehometest.core.data.mapper.toSerializable
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

internal class CityRepositoryImpl @Inject constructor(
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val dataStore: DataStore<SavedCitiesState>,
) : CityRepository {

    override fun observeSavedCities(): Flow<List<City>> =
        dataStore.data.map { state -> state.cities.map { it.toCity() } }

    override fun observeSelectedCity(): Flow<City?> =
        dataStore.data.map { state ->
            state.cities.firstOrNull { it.id == state.selectedCityId }?.toCity()
        }

    override suspend fun searchCities(query: String): List<City> = try {
        geocodingApi.search(name = query.trim())
            .results.map { it.toCity() }
    } catch (e: HttpException) {
        throw IOException("HTTP ${e.code()}", e)
    }

    override suspend fun addCity(city: City) {
        dataStore.updateData { state ->
            if (state.cities.any { it.id == city.id }) {
                state
            } else {
                state.copy(
                    cities = state.cities + city.toSerializable(),
                    selectedCityId = state.selectedCityId ?: city.id,
                )
            }
        }
    }

    override suspend fun removeCity(cityId: Long) {
        dataStore.updateData { state ->
            val remaining = state.cities.filterNot { it.id == cityId }
            val newSelected = when {
                state.selectedCityId != cityId -> state.selectedCityId
                remaining.isNotEmpty() -> remaining.first().id
                else -> null
            }
            state.copy(cities = remaining, selectedCityId = newSelected)
        }
    }

    override suspend fun setSelectedCity(cityId: Long) {
        dataStore.updateData { it.copy(selectedCityId = cityId) }
    }
}
