package com.opnt.takehometest.feature.weather.addcity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddCityViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
    observeSavedCities: ObserveSavedCitiesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val _events = MutableSharedFlow<AddCityEvent>(
        replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AddCityEvent> = _events.asSharedFlow()

    private val searchFlow: Flow<AddCityUiState> = query
        .map { it.trim() }
        .debounce(DEBOUNCE_DURATION)
        .distinctUntilChanged()
        .transformLatest { q ->
            if (q.isEmpty()) {
                emit(AddCityUiState.Idle)
            } else {
                emit(AddCityUiState.Loading)
                emit(search(q))
            }
        }

    val uiState: StateFlow<AddCityUiState> = combine(
        searchFlow,
        observeSavedCities(),
    ) { base, saved -> base.markSaved(saved) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddCityUiState.Idle)

    fun onQueryChange(q: String) { query.value = q }

    fun onAddCity(city: City) {
        viewModelScope.launch {
            try {
                addCity(city)
                _events.tryEmit(AddCityEvent.CityAdded)
            } catch (_: Exception) {
                _events.tryEmit(AddCityEvent.AddFailed)
            }
        }
    }

    private suspend fun search(q: String): AddCityUiState = try {
        val cities = searchCities(q)
        if (cities.isEmpty()) AddCityUiState.NoResults
        else AddCityUiState.Results(cities.map { AddCityResultItem(it, alreadySaved = false) })
    } catch (_: IOException) {
        AddCityUiState.Error(AddCityError.SearchFailed)
    }

    private fun AddCityUiState.markSaved(saved: List<City>): AddCityUiState {
        if (this !is AddCityUiState.Results) return this
        val savedIds = saved.mapTo(mutableSetOf()) { it.id }
        return copy(cities = cities.map { it.copy(alreadySaved = it.city.id in savedIds) })
    }

    companion object {
        val DEBOUNCE_DURATION = 300.milliseconds
    }
}
