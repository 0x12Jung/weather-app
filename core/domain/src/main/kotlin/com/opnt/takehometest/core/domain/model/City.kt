package com.opnt.takehometest.core.domain.model

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val admin: String?,
    val coordinates: Coordinates,
    val timezone: String,
)
