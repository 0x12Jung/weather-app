package com.opnt.takehometest.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.ui.R

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painterResource(id = condition.drawableRes()),
        contentDescription = condition.label(),
        modifier = modifier,
        colorFilter = colorFilter,
    )
}

fun WeatherCondition.label(): String = when (this) {
    WeatherCondition.Clear -> "Clear"
    WeatherCondition.PartlyCloudy -> "Partly cloudy"
    WeatherCondition.Cloudy -> "Overcast"
    WeatherCondition.Fog -> "Fog"
    WeatherCondition.Drizzle -> "Drizzle"
    WeatherCondition.Rain -> "Rain"
    WeatherCondition.Snow -> "Snow"
    WeatherCondition.Thunderstorm -> "Thunderstorm"
    is WeatherCondition.Unknown -> "Unknown conditions (code $wmoCode)"
}

@DrawableRes
private fun WeatherCondition.drawableRes(): Int = when (this) {
    WeatherCondition.Clear -> R.drawable.ic_weather_clear
    WeatherCondition.PartlyCloudy -> R.drawable.ic_weather_partly_cloudy
    WeatherCondition.Cloudy -> R.drawable.ic_weather_cloudy
    WeatherCondition.Fog -> R.drawable.ic_weather_fog
    WeatherCondition.Drizzle -> R.drawable.ic_weather_drizzle
    WeatherCondition.Rain -> R.drawable.ic_weather_rain
    WeatherCondition.Snow -> R.drawable.ic_weather_snow
    WeatherCondition.Thunderstorm -> R.drawable.ic_weather_thunder
    is WeatherCondition.Unknown -> R.drawable.ic_weather_unknown
}
