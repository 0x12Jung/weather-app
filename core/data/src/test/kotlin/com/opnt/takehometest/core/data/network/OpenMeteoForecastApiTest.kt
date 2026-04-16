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

class OpenMeteoForecastApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenMeteoForecastApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OpenMeteoForecastApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `parses realistic Taipei forecast fixture`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/forecast_response_taipei.json")!!.readText()
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )

        val response = api.getForecast(
            lat = 25.0330,
            lon = 121.5654,
            timezone = "Asia/Taipei",
        )

        assertThat(response.timezone).isEqualTo("Asia/Taipei")
        assertThat(response.current.temperature2m).isEqualTo(22.5)
        assertThat(response.current.weatherCode).isEqualTo(2)
        assertThat(response.current.isDay).isEqualTo(1)
        assertThat(response.hourly.time).hasSize(25)
        assertThat(response.daily.weatherCode).containsExactly(2, 61, 61, 3, 0, 0, 1).inOrder()
    }
}
