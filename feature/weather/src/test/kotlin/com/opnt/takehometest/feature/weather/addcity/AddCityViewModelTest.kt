package com.opnt.takehometest.feature.weather.addcity

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCityViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val search: SearchCitiesUseCase = mockk()
    private val add: AddCityUseCase = mockk()
    private val observeSaved: ObserveSavedCitiesUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )

    private fun newVm(): AddCityViewModel {
        every { observeSaved() } returns flowOf(emptyList())
        return AddCityViewModel(search, add, observeSaved)
    }

    @Test
    fun `emits Idle when query is below minimum length`() = runTest {
        val vm = newVm()
        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(AddCityUiState.Idle::class.java)
            vm.onQueryChange("a")
            advanceUntilIdle()
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounces rapid queries and only searches the latest`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        val vm = newVm()
        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(AddCityUiState.Idle::class.java)

            vm.onQueryChange("Ta")
            advanceTimeBy(100)
            vm.onQueryChange("Tai")
            advanceTimeBy(100)
            vm.onQueryChange("Taipei")
            advanceUntilIdle()

            coVerify(exactly = 1) { search("Taipei") }
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Results::class.java)
            assertThat((latest as AddCityUiState.Results).cities.map { it.city })
                .containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAddCity invokes AddCityUseCase and emits CityAdded event`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } just Runs

        vm.events.test {
            vm.onAddCity(taipei)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(AddCityEvent.CityAdded)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { add(taipei) }
    }

    @Test
    fun `marks already-saved cities via alreadySaved flag`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        every { observeSaved() } returns flowOf(listOf(taipei))
        val vm = AddCityViewModel(search, add, observeSaved)

        vm.uiState.test {
            skipItems(1)
            vm.onQueryChange("Taipei")
            advanceUntilIdle()
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Results::class.java)
            val firstResult = (latest as AddCityUiState.Results).cities.first()
            assertThat(firstResult.alreadySaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
