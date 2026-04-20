package com.opnt.takehometest.core.data.di

import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton @ForecastRetrofit
    fun provideForecastRetrofit(client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit("https://api.open-meteo.com/", client, json)

    @Provides @Singleton @GeocodingRetrofit
    fun provideGeocodingRetrofit(client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit("https://geocoding-api.open-meteo.com/", client, json)

    private fun buildRetrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideForecastApi(@ForecastRetrofit retrofit: Retrofit): OpenMeteoForecastApi =
        retrofit.create(OpenMeteoForecastApi::class.java)

    @Provides @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): OpenMeteoGeocodingApi =
        retrofit.create(OpenMeteoGeocodingApi::class.java)
}
