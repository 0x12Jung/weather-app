package com.opnt.takehometest.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import org.junit.Test

class CityMapperTest {

    @Test
    fun `geocoding DTO to domain preserves id, name, country, admin, coords, timezone`() {
        val dto = GeocodingResponseDto.GeocodingResultDto(
            id = 1668341L,
            name = "Taipei",
            latitude = 25.04,
            longitude = 121.56,
            country = "Taiwan",
            admin1 = "Taipei City",
            timezone = "Asia/Taipei",
        )
        assertThat(dto.toCity()).isEqualTo(City(
            id = 1668341L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
            coordinates = Coordinates(25.04, 121.56), timezone = "Asia/Taipei",
        ))
    }

    @Test
    fun `serializable to domain round-trip preserves fields`() {
        val city = City(
            id = 99L, name = "London", country = "UK", admin = null,
            coordinates = Coordinates(51.5, -0.12), timezone = "Europe/London",
        )
        assertThat(city.toSerializable().toCity()).isEqualTo(city)
    }

    @Test
    fun `serializable to domain preserves null admin`() {
        val serialized = SerializableCity(
            id = 99L, name = "X", country = "Y", admin = null,
            latitude = 0.0, longitude = 0.0, timezone = "UTC",
        )
        assertThat(serialized.toCity().admin).isNull()
    }
}
