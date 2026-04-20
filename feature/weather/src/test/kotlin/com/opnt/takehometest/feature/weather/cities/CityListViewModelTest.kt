package com.opnt.takehometest.feature.weather.cities

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.core.domain.usecase.RemoveCityUseCase
import com.opnt.takehometest.core.domain.usecase.SelectCityUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val observeSaved: ObserveSavedCitiesUseCase = mockk()
    private val observeSelected: ObserveSelectedCityUseCase = mockk()
    private val select: SelectCityUseCase = mockk()
    private val remove: RemoveCityUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val tokyo = City(
        id = 2L, name = "Tokyo", country = "Japan", admin = null,
        coordinates = Coordinates(35.6, 139.7), timezone = "Asia/Tokyo",
    )

    private fun newVm(
        savedFlow: MutableStateFlow<List<City>> = MutableStateFlow(emptyList()),
        selectedFlow: MutableStateFlow<City?> = MutableStateFlow(null),
    ): Pair<CityListViewModel, Pair<MutableStateFlow<List<City>>, MutableStateFlow<City?>>> {
        every { observeSaved() } returns savedFlow
        every { observeSelected() } returns selectedFlow
        return CityListViewModel(observeSaved, observeSelected, select, remove) to
            (savedFlow to selectedFlow)
    }

    @Test
    fun `emits Content combining saved cities and selection`() = runTest {
        val savedFlow = MutableStateFlow(listOf(taipei, tokyo))
        val selectedFlow = MutableStateFlow<City?>(tokyo)
        val (vm, _) = newVm(savedFlow, selectedFlow)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(CityListUiState.Loading)
            val content = awaitItem()
            assertThat(content).isInstanceOf(CityListUiState.Content::class.java)
            val items = (content as CityListUiState.Content).cities
            assertThat(items.map { it.city }).containsExactly(taipei, tokyo).inOrder()
            assertThat(items.single { it.city == tokyo }.isSelected).isTrue()
            assertThat(items.single { it.city == taipei }.isSelected).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSelectCity invokes SelectCityUseCase`() = runTest {
        val (vm, _) = newVm()
        coEvery { select(42L) } just Runs
        vm.onSelectCity(42L)
        advanceUntilIdle()
        coVerify(exactly = 1) { select(42L) }
    }

    @Test
    fun `onSwipeRemove removes city immediately`() = runTest {
        val (vm, _) = newVm(MutableStateFlow(listOf(taipei, tokyo)))
        coEvery { remove(tokyo.id) } just Runs

        vm.onSwipeRemove(tokyo)
        advanceUntilIdle()

        coVerify(exactly = 1) { remove(tokyo.id) }
    }
}
