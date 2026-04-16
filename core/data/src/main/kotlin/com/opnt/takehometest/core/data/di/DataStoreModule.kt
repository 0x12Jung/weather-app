package com.opnt.takehometest.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.opnt.takehometest.core.data.local.SavedCitiesSerializer
import com.opnt.takehometest.core.data.local.SavedCitiesState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.datetime.Clock

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides @Singleton
    internal fun provideSavedCitiesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<SavedCitiesState> = DataStoreFactory.create(
        serializer = SavedCitiesSerializer,
        produceFile = { context.dataStoreFile("saved_cities.json") },
    )

    @Provides @Singleton
    fun provideClock(): Clock = Clock.System
}
