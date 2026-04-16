package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchCitiesUseCaseTest {

    private val repo: CityRepository = mockk()
    private val useCase = SearchCitiesUseCase(repo)

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0330, 121.5654), timezone = "Asia/Taipei",
    )

    @Test
    fun `trimmed query shorter than 2 returns empty without hitting repo`() = runTest {
        assertThat(useCase("")).isEmpty()
        assertThat(useCase(" ")).isEmpty()
        assertThat(useCase("a")).isEmpty()
        assertThat(useCase(" a ")).isEmpty()
        coVerify(exactly = 0) { repo.searchCities(any()) }
    }

    @Test
    fun `trimmed query of length 2 or more delegates to repository`() = runTest {
        coEvery { repo.searchCities("Taipei") } returns listOf(taipei)
        val result = useCase("  Taipei  ")
        assertThat(result).containsExactly(taipei)
        coVerify(exactly = 1) { repo.searchCities("Taipei") }
    }
}
