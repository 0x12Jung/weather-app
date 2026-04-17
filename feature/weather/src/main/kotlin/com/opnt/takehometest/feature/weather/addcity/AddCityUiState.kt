package com.opnt.takehometest.feature.weather.addcity

import com.opnt.takehometest.core.domain.model.City

sealed interface AddCityUiState {
    val query: String
    data class Idle(override val query: String = "") : AddCityUiState
    data class Loading(override val query: String) : AddCityUiState
    data class Results(
        override val query: String,
        val cities: List<AddCityResultItem>,
    ) : AddCityUiState
    data class NoResults(override val query: String) : AddCityUiState
    data class Error(override val query: String, val message: String) : AddCityUiState
}

data class AddCityResultItem(val city: City, val alreadySaved: Boolean)

sealed interface AddCityEvent {
    data object CityAdded : AddCityEvent
}
