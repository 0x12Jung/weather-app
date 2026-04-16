package com.opnt.takehometest.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.local.SavedCitiesState
import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.mapper.CityMapper
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CityRepositoryImplTest {

    private val api: OpenMeteoGeocodingApi = mockk()
    private val mapper = CityMapper()

    private fun newRepo(initial: SavedCitiesState = SavedCitiesState()) =
        CityRepositoryImpl(api, FakeDataStore(initial), mapper)

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val tokyo = City(
        id = 2L, name = "Tokyo", country = "Japan", admin = null,
        coordinates = Coordinates(35.6, 139.7), timezone = "Asia/Tokyo",
    )

    @Test
    fun `observeSavedCities maps persisted state to domain`() = runTest {
        val repo = newRepo(SavedCitiesState(
            cities = listOf(
                SerializableCity(1L, "Taipei", "Taiwan", "Taipei City", 25.0, 121.5, "Asia/Taipei"),
            ),
        ))
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeSelectedCity returns null when id not in list`() = runTest {
        val repo = newRepo(SavedCitiesState(
            cities = emptyList(), selectedCityId = 99L,
        ))
        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCity inserts and auto-selects first city`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isEqualTo(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCity is idempotent on duplicate id`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.addCity(taipei.copy(name = "Taipei City"))
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCity updates selectedCityId when removing selected city`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.addCity(tokyo)
        repo.setSelectedCity(tokyo.id)

        repo.removeCity(tokyo.id)

        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isEqualTo(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCity last city sets selectedCityId to null`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.removeCity(taipei.id)

        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchCities trims and maps API results`() = runTest {
        coEvery { api.search(name = "Taipei") } returns GeocodingResponseDto(
            results = listOf(
                GeocodingResponseDto.GeocodingResultDto(
                    id = 1L, name = "Taipei", latitude = 25.0, longitude = 121.5,
                    country = "Taiwan", admin1 = "Taipei City", timezone = "Asia/Taipei",
                )
            )
        )
        val repo = newRepo()

        val result = repo.searchCities("  Taipei  ")

        assertThat(result).containsExactly(taipei)
    }
}
