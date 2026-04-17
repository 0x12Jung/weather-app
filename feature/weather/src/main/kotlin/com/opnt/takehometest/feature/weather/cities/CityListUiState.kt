package com.opnt.takehometest.feature.weather.cities

import com.opnt.takehometest.core.domain.model.City

sealed interface CityListUiState {
    data object Loading : CityListUiState
    data class Content(
        val cities: List<CityListItem>,
        val selectedCityId: Long?,
    ) : CityListUiState
}

data class CityListItem(val city: City, val isSelected: Boolean)
