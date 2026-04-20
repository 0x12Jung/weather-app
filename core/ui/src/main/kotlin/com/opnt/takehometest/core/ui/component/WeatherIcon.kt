package com.opnt.takehometest.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.ui.R

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = condition.imageVector(),
        contentDescription = condition.label(),
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun WeatherCondition.label(): String = when (this) {
    WeatherCondition.Clear -> stringResource(R.string.core_ui_weather_condition_clear)
    WeatherCondition.PartlyCloudy -> stringResource(R.string.core_ui_weather_condition_partly_cloudy)
    WeatherCondition.Cloudy -> stringResource(R.string.core_ui_weather_condition_cloudy)
    WeatherCondition.Fog -> stringResource(R.string.core_ui_weather_condition_fog)
    WeatherCondition.Drizzle -> stringResource(R.string.core_ui_weather_condition_drizzle)
    WeatherCondition.Rain -> stringResource(R.string.core_ui_weather_condition_rain)
    WeatherCondition.Snow -> stringResource(R.string.core_ui_weather_condition_snow)
    WeatherCondition.Thunderstorm -> stringResource(R.string.core_ui_weather_condition_thunderstorm)
    is WeatherCondition.Unknown -> stringResource(R.string.core_ui_weather_condition_unknown, wmoCode)
}

private fun WeatherCondition.imageVector(): ImageVector = when (this) {
    WeatherCondition.Clear -> Icons.Filled.WbSunny
    WeatherCondition.PartlyCloudy -> Icons.Filled.WbCloudy
    WeatherCondition.Cloudy -> Icons.Filled.Cloud
    WeatherCondition.Fog -> Icons.Filled.BlurOn
    WeatherCondition.Drizzle -> Icons.Filled.Grain
    WeatherCondition.Rain -> Icons.Filled.Umbrella
    WeatherCondition.Snow -> Icons.Filled.AcUnit
    WeatherCondition.Thunderstorm -> Icons.Filled.Thunderstorm
    is WeatherCondition.Unknown -> Icons.AutoMirrored.Filled.HelpOutline
}
