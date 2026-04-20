package com.opnt.takehometest.feature.weather.addcity

import com.opnt.takehometest.core.domain.model.City

sealed interface AddCityUiState {
    data object Idle : AddCityUiState
    data object Loading : AddCityUiState
    data class Results(val cities: List<AddCityResultItem>) : AddCityUiState
    data object NoResults : AddCityUiState
    data class Error(val message: String) : AddCityUiState
}

data class AddCityResultItem(val city: City, val alreadySaved: Boolean)

sealed interface AddCityEvent {
    data object CityAdded : AddCityEvent
}
