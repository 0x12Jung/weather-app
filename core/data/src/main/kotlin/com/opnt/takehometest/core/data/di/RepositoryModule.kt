package com.opnt.takehometest.core.data.di

import com.opnt.takehometest.core.data.repository.CityRepositoryImpl
import com.opnt.takehometest.core.data.repository.WeatherRepositoryImpl
import com.opnt.takehometest.core.domain.repository.CityRepository
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    internal abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl,
    ): WeatherRepository

    @Binds @Singleton
    internal abstract fun bindCityRepository(
        impl: CityRepositoryImpl,
    ): CityRepository
}
