package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CityMutationUseCasesTest {

    private val repo: CityRepository = mockk()

    private val sampleCity = City(
        id = 1668341L,
        name = "Taipei",
        country = "Taiwan",
        admin = "Taipei City",
        coordinates = Coordinates(25.0330, 121.5654),
        timezone = "Asia/Taipei",
    )

    @Test
    fun `ObserveSavedCities re-emits repository flow`() = runTest {
        every { repo.observeSavedCities() } returns flowOf(listOf(sampleCity))
        val result = ObserveSavedCitiesUseCase(repo)().toList()
        assertThat(result).containsExactly(listOf(sampleCity))
    }

    @Test
    fun `ObserveSelectedCity re-emits repository flow`() = runTest {
        every { repo.observeSelectedCity() } returns flowOf(sampleCity)
        val result = ObserveSelectedCityUseCase(repo)().toList()
        assertThat(result).containsExactly(sampleCity)
    }

    @Test
    fun `AddCityUseCase delegates to repository`() = runTest {
        coEvery { repo.addCity(sampleCity) } just Runs
        AddCityUseCase(repo)(sampleCity)
        coVerify(exactly = 1) { repo.addCity(sampleCity) }
    }

    @Test
    fun `RemoveCityUseCase delegates by id`() = runTest {
        coEvery { repo.removeCity(42L) } just Runs
        RemoveCityUseCase(repo)(42L)
        coVerify(exactly = 1) { repo.removeCity(42L) }
    }

    @Test
    fun `SelectCityUseCase delegates by id`() = runTest {
        coEvery { repo.setSelectedCity(42L) } just Runs
        SelectCityUseCase(repo)(42L)
        coVerify(exactly = 1) { repo.setSelectedCity(42L) }
    }
}
