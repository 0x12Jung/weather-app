package com.opnt.takehometest.core.data.mapper

import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates

internal fun GeocodingResponseDto.GeocodingResultDto.toCity(): City = City(
    id = id,
    name = name,
    country = country,
    admin = admin1,
    coordinates = Coordinates(latitude, longitude),
    timezone = timezone,
)

internal fun SerializableCity.toCity(): City = City(
    id = id,
    name = name,
    country = country,
    admin = admin,
    coordinates = Coordinates(latitude, longitude),
    timezone = timezone,
)

internal fun City.toSerializable(): SerializableCity = SerializableCity(
    id = id,
    name = name,
    country = country,
    admin = admin,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
    timezone = timezone,
)
