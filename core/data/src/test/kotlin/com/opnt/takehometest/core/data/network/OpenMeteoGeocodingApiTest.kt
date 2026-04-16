package com.opnt.takehometest.core.data.network

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OpenMeteoGeocodingApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenMeteoGeocodingApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `parses standard geocoding response`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/geocoding_response_taipei.json")!!.readText()
        server.enqueue(MockResponse().setResponseCode(200)
            .setHeader("Content-Type", "application/json").setBody(body))

        val response = api.search(name = "Taipei")

        assertThat(response.results).hasSize(2)
        assertThat(response.results[0].id).isEqualTo(1668341L)
        assertThat(response.results[0].admin1).isEqualTo("Taipei City")
    }

    @Test
    fun `parses response with missing results field as empty list`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/geocoding_response_empty.json")!!.readText()
        server.enqueue(MockResponse().setResponseCode(200)
            .setHeader("Content-Type", "application/json").setBody(body))

        val response = api.search(name = "zzzzz")

        assertThat(response.results).isEmpty()
    }
}
