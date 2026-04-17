package com.opnt.takehometest

import android.app.Application
import com.opnt.takehometest.core.domain.usecase.SeedDefaultCityUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class TakeHomeTestApplication : Application() {

    @Inject lateinit var seedDefaultCity: SeedDefaultCityUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedDefaultCity() }
    }
}
