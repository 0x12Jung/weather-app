package com.opnt.takehometest.core.data.mapper

import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import javax.inject.Inject

class CityMapper @Inject constructor() {

    fun toDomain(dto: GeocodingResponseDto.GeocodingResultDto): City = City(
        id = dto.id,
        name = dto.name,
        country = dto.country,
        admin = dto.admin1,
        coordinates = Coordinates(dto.latitude, dto.longitude),
        timezone = dto.timezone,
    )

    internal fun toSerializable(city: City): SerializableCity = SerializableCity(
        id = city.id,
        name = city.name,
        country = city.country,
        admin = city.admin,
        latitude = city.coordinates.latitude,
        longitude = city.coordinates.longitude,
        timezone = city.timezone,
    )

    internal fun toDomain(saved: SerializableCity): City = City(
        id = saved.id,
        name = saved.name,
        country = saved.country,
        admin = saved.admin,
        coordinates = Coordinates(saved.latitude, saved.longitude),
        timezone = saved.timezone,
    )
}
