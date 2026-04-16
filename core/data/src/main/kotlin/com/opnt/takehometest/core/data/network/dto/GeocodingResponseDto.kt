package com.opnt.takehometest.core.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList(),
) {
    @Serializable
    data class GeocodingResultDto(
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val country: String,
        val admin1: String? = null,
        val timezone: String,
    )
}
