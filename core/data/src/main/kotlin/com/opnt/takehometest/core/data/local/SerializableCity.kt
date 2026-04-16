package com.opnt.takehometest.core.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class SerializableCity(
    val id: Long,
    val name: String,
    val country: String,
    val admin: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)
