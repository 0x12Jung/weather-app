package com.opnt.takehometest.feature.weather.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.weatherGraph(navController: NavController) {
    composable<WeatherRoute> { Text("Weather screen stub") }
    composable<CityListRoute> { Text("City list screen stub") }
    composable<AddCityRoute> { Text("Add city screen stub") }
}
