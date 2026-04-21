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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddCityViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
    observeSavedCities: ObserveSavedCitiesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val transientState = MutableStateFlow(AddCityTransientState())

    private val searchFlow: Flow<AddCitySearchState> = query
        .map { it.trim() }
        .debounce(DEBOUNCE_DURATION)
        .distinctUntilChanged()
        .transformLatest { q ->
            if (q.isEmpty()) {
                emit(AddCitySearchState.Idle)
            } else {
                emit(AddCitySearchState.Loading)
                emit(search(q))
            }
        }

    val uiState: StateFlow<AddCityUiState> = combine(
        searchFlow,
        observeSavedCities(),
        transientState,
    ) { searchState, saved, transient ->
        AddCityUiState(
            searchState = searchState.markSaved(saved),
            showAddFailedMsg = transient.showAddFailedMsg,
            isAdded = transient.isAdded,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddCityUiState())

    fun onQueryChange(q: String) { query.value = q }

    fun onAddCity(city: City) {
        viewModelScope.launch {
            try {
                addCity(city)
                transientState.update { it.copy(isAdded = true) }
            } catch (_: Exception) {
                transientState.update { it.copy(showAddFailedMsg = true) }
            }
        }
    }

    fun onNavigationHandled() {
        transientState.update { it.copy(isAdded = false) }
    }

    fun onAddFailedMessageShown() {
        transientState.update { it.copy(showAddFailedMsg = false) }
    }

    private suspend fun search(q: String): AddCitySearchState = try {
        val cities = searchCities(q)
        if (cities.isEmpty()) AddCitySearchState.NoResults
        else AddCitySearchState.Results(cities.map { AddCityResultItem(it, alreadySaved = false) })
    } catch (_: IOException) {
        AddCitySearchState.Error(AddCityError.SearchFailed)
    }

    private fun AddCitySearchState.markSaved(saved: List<City>): AddCitySearchState {
        if (this !is AddCitySearchState.Results) return this
        val savedIds = saved.mapTo(mutableSetOf()) { it.id }
        return copy(cities = cities.map { it.copy(alreadySaved = it.city.id in savedIds) })
    }

    companion object {
        val DEBOUNCE_DURATION = 300.milliseconds
    }
}
