package com.opnt.takehometest.core.data.local

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SavedCitiesSerializerTest {

    @Test
    fun `default value is empty cities with null selectedCityId`() {
        assertThat(SavedCitiesSerializer.defaultValue.cities).isEmpty()
        assertThat(SavedCitiesSerializer.defaultValue.selectedCityId).isNull()
    }

    @Test
    fun `write then read round-trips state faithfully`() = runTest {
        val state = SavedCitiesState(
            cities = listOf(
                SerializableCity(1L, "Taipei", "Taiwan", "Taipei City", 25.0, 121.5, "Asia/Taipei"),
                SerializableCity(2L, "Tokyo", "Japan", null, 35.6, 139.7, "Asia/Tokyo"),
            ),
            selectedCityId = 2L,
        )
        val out = ByteArrayOutputStream()
        SavedCitiesSerializer.writeTo(state, out)

        val input = ByteArrayInputStream(out.toByteArray())
        val read = SavedCitiesSerializer.readFrom(input)

        assertThat(read).isEqualTo(state)
    }

    @Test(expected = CorruptionException::class)
    fun `malformed JSON throws CorruptionException`() = runTest {
        val malformed = "{this is not json".toByteArray()
        SavedCitiesSerializer.readFrom(ByteArrayInputStream(malformed))
    }
}
