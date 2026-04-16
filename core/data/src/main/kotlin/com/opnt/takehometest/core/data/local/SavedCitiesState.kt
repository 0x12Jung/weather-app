package com.opnt.takehometest.core.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class SavedCitiesState(
    val cities: List<SerializableCity> = emptyList(),
    val selectedCityId: Long? = null,
)
