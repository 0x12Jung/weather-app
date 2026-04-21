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
    fun `returns to Idle when query is cleared`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        val vm = newVm()
        vm.uiState.test {
            skipItems(1)
            vm.onQueryChange("Taipei")
            advanceUntilIdle()
            vm.onQueryChange("   ")
            advanceUntilIdle()
            val latest = expectMostRecentItem()
            assertThat(latest.searchState).isEqualTo(AddCitySearchState.Idle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounces rapid queries and only searches the latest`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        val vm = newVm()
        vm.uiState.test {
            assertThat(awaitItem().searchState).isEqualTo(AddCitySearchState.Idle)

            vm.onQueryChange("Ta")
            advanceTimeBy(100)
            vm.onQueryChange("Tai")
            advanceTimeBy(100)
            vm.onQueryChange("Taipei")
            advanceUntilIdle()

            coVerify(exactly = 1) { search("Taipei") }
            val latest = expectMostRecentItem()
            assertThat(latest.searchState).isInstanceOf(AddCitySearchState.Results::class.java)
            assertThat((latest.searchState as AddCitySearchState.Results).cities.map { it.city })
                .containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAddCity invokes AddCityUseCase and sets isAdded`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } just Runs

        vm.uiState.test {
            assertThat(awaitItem().isAdded).isFalse()
            vm.onAddCity(taipei)
            advanceUntilIdle()
            assertThat(expectMostRecentItem().isAdded).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { add(taipei) }
    }

    @Test
    fun `onNavigationHandled clears isAdded`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } just Runs

        vm.onAddCity(taipei)
        advanceUntilIdle()
        vm.onNavigationHandled()

        vm.uiState.test {
            assertThat(awaitItem().isAdded).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAddCity failure sets showAddFailedMsg`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } throws IllegalStateException()

        vm.uiState.test {
            assertThat(awaitItem().showAddFailedMsg).isFalse()
            vm.onAddCity(taipei)
            advanceUntilIdle()
            assertThat(expectMostRecentItem().showAddFailedMsg).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAddFailedMessageShown clears showAddFailedMsg`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } throws IllegalStateException()

        vm.onAddCity(taipei)
        advanceUntilIdle()
        vm.onAddFailedMessageShown()

        vm.uiState.test {
            assertThat(awaitItem().showAddFailedMsg).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when search throws IOException`() = runTest {
        coEvery { search("Tai") } throws java.io.IOException()
        val vm = newVm()
        vm.uiState.test {
            skipItems(1)
            vm.onQueryChange("Tai")
            advanceUntilIdle()
            val latest = expectMostRecentItem().searchState as AddCitySearchState.Error
            assertThat(latest.error).isEqualTo(AddCityError.SearchFailed)
            cancelAndIgnoreRemainingEvents()
        }
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
            assertThat(latest.searchState).isInstanceOf(AddCitySearchState.Results::class.java)
            val firstResult = (latest.searchState as AddCitySearchState.Results).cities.first()
            assertThat(firstResult.alreadySaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
