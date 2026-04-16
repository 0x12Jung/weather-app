package com.opnt.takehometest.core.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
internal object SavedCitiesSerializer : Serializer<SavedCitiesState> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: SavedCitiesState = SavedCitiesState()

    override suspend fun readFrom(input: InputStream): SavedCitiesState =
        try {
            json.decodeFromStream(input)
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read SavedCitiesState", e)
        }

    override suspend fun writeTo(t: SavedCitiesState, output: OutputStream) {
        json.encodeToStream(t, output)
    }
}
