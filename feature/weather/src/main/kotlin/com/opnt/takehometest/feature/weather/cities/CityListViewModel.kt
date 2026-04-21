package com.opnt.takehometest.feature.weather.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.core.domain.usecase.RemoveCityUseCase
import com.opnt.takehometest.core.domain.usecase.SelectCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CityListViewModel @Inject constructor(
    observeSavedCities: ObserveSavedCitiesUseCase,
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val selectCity: SelectCityUseCase,
    private val removeCity: RemoveCityUseCase,
) : ViewModel() {

    val uiState: StateFlow<CityListUiState> =
        combine(observeSavedCities(), observeSelectedCity()) { cities, selected ->
            CityListUiState.Content(
                cities = cities.map { CityListItem(it, it.id == selected?.id) },
                selectedCityId = selected?.id,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityListUiState.Loading)

    fun onSelectCity(cityId: Long) {
        viewModelScope.launch { selectCity(cityId) }
    }

    fun onSwipeRemove(city: com.opnt.takehometest.core.domain.model.City) {
        viewModelScope.launch {
            removeCity(city.id)
        }
    }
}
