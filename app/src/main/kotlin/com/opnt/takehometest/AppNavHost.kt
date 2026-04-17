package com.opnt.takehometest

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.opnt.takehometest.feature.weather.navigation.WeatherRoute
import com.opnt.takehometest.feature.weather.navigation.weatherGraph

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = WeatherRoute,
    ) {
        weatherGraph(navController)
    }
}
