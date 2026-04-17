package com.opnt.takehometest.feature.weather.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.opnt.takehometest.feature.weather.addcity.AddCityScreen
import com.opnt.takehometest.feature.weather.cities.CityListScreen
import com.opnt.takehometest.feature.weather.weather.WeatherScreen

fun NavGraphBuilder.weatherGraph(navController: NavController) {
    composable<WeatherRoute> {
        WeatherScreen(onOpenCityList = { navController.navigate(CityListRoute) })
    }
    composable<CityListRoute> {
        CityListScreen(
            onAddCity = { navController.navigate(AddCityRoute) },
            onBack = { navController.popBackStack() },
            onCitySelected = { navController.popBackStack() },
        )
    }
    composable<AddCityRoute> {
        AddCityScreen(
            onCityAdded = { navController.popBackStack() },
            onBack = { navController.popBackStack() },
        )
    }
}
