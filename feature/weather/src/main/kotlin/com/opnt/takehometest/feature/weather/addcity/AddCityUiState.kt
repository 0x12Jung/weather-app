package com.opnt.takehometest.feature.weather.addcity

import com.opnt.takehometest.core.domain.model.City

data class AddCityUiState(
    val searchState: AddCitySearchState = AddCitySearchState.Idle,
    val showAddFailedMsg: Boolean = false,
    val isAdded: Boolean = false,
)

sealed interface AddCitySearchState {
    data object Idle : AddCitySearchState
    data object Loading : AddCitySearchState
    data class Results(val cities: List<AddCityResultItem>) : AddCitySearchState
    data object NoResults : AddCitySearchState
    data class Error(val error: AddCityError) : AddCitySearchState
}

enum class AddCityError { SearchFailed }

data class AddCityResultItem(val city: City, val alreadySaved: Boolean)

data class AddCityTransientState(
    val showAddFailedMsg: Boolean = false,
    val isAdded: Boolean = false,
)
