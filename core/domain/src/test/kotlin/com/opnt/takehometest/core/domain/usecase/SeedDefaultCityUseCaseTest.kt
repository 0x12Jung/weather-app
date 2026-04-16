package com.opnt.takehometest.core.domain.usecase

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
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SeedDefaultCityUseCaseTest {

    private val repo: CityRepository = mockk()
    private val useCase = SeedDefaultCityUseCase(repo)

    @Test
    fun `inserts Taipei and selects it when saved list is empty`() = runTest {
        every { repo.observeSavedCities() } returns flowOf(emptyList())
        coEvery { repo.addCity(any()) } just Runs
        coEvery { repo.setSelectedCity(any()) } just Runs

        useCase()

        coVerify(exactly = 1) {
            repo.addCity(match {
                it.id == 1668341L &&
                    it.name == "Taipei" &&
                    it.coordinates == Coordinates(25.0330, 121.5654) &&
                    it.timezone == "Asia/Taipei"
            })
        }
        coVerify(exactly = 1) { repo.setSelectedCity(1668341L) }
    }

    @Test
    fun `does nothing when saved list is non-empty`() = runTest {
        val existing = City(
            id = 999L, name = "London", country = "UK", admin = null,
            coordinates = Coordinates(51.5, -0.12), timezone = "Europe/London",
        )
        every { repo.observeSavedCities() } returns flowOf(listOf(existing))

        useCase()

        coVerify(exactly = 0) { repo.addCity(any()) }
        coVerify(exactly = 0) { repo.setSelectedCity(any()) }
    }
}
