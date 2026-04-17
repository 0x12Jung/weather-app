package com.opnt.takehometest.feature.weather.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.core.domain.usecase.RemoveCityUseCase
import com.opnt.takehometest.core.domain.usecase.SelectCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CityListViewModel @Inject constructor(
    observeSavedCities: ObserveSavedCitiesUseCase,
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val selectCity: SelectCityUseCase,
    private val removeCity: RemoveCityUseCase,
    private val addCity: AddCityUseCase,
) : ViewModel() {

    val uiState: StateFlow<CityListUiState> =
        combine(observeSavedCities(), observeSelectedCity()) { cities, selected ->
            CityListUiState.Content(
                cities = cities.map { CityListItem(it, it.id == selected?.id) },
                selectedCityId = selected?.id,
            ) as CityListUiState
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityListUiState.Loading)

    private val _pendingUndo = MutableStateFlow<City?>(null)
    val pendingUndo: StateFlow<City?> = _pendingUndo.asStateFlow()

    fun onSelectCity(cityId: Long) {
        viewModelScope.launch { selectCity(cityId) }
    }

    fun onSwipeRemove(city: City) {
        viewModelScope.launch {
            removeCity(city.id)
            _pendingUndo.value = city
        }
    }

    fun onUndoRemoval() {
        val city = _pendingUndo.value ?: return
        viewModelScope.launch {
            addCity(city)
            _pendingUndo.value = null
        }
    }

    fun onUndoConsumed() {
        _pendingUndo.value = null
    }
}
