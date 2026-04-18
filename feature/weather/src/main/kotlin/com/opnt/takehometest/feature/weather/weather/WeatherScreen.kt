package com.opnt.takehometest.feature.weather.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opnt.takehometest.core.domain.model.DailyWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.HourlyWeather
import com.opnt.takehometest.core.ui.component.EmptyView
import com.opnt.takehometest.core.ui.component.ErrorView
import com.opnt.takehometest.core.ui.component.LoadingIndicator
import com.opnt.takehometest.core.ui.component.WeatherIcon
import com.opnt.takehometest.core.ui.component.label
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onOpenCityList: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? WeatherUiState.Success)?.city?.let { "${it.name}, ${it.country}" }
                        ?: "Weather"
                    Text(title)
                },
                actions = {
                    IconButton(onClick = onOpenCityList) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Cities")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                WeatherUiState.Loading -> LoadingIndicator()
                WeatherUiState.NoCity -> EmptyView(
                    message = "No city selected. Add a city to see forecasts.",
                    actionLabel = "Add city",
                    onAction = onOpenCityList,
                )
                is WeatherUiState.Error -> ErrorView(state.message, onRetry = viewModel::onRetry)
                is WeatherUiState.Success -> WeatherContent(state.forecast)
            }
        }
    }
}

@Composable
private fun WeatherContent(forecast: Forecast) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CurrentCard(forecast) }
        item { Text("Next 24 hours", style = MaterialTheme.typography.titleMedium) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(forecast.hourly, key = { it.time.toEpochMilliseconds() }) { HourlyCell(it) }
            }
        }
        item { HorizontalDivider() }
        item { Text("Next 7 days", style = MaterialTheme.typography.titleMedium) }
        items(forecast.daily, key = { it.date.toEpochDays() }) { DailyRow(it) }
    }
}

@Composable
private fun CurrentCard(forecast: Forecast) {
    val current = forecast.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WeatherIcon(current.condition, modifier = Modifier.size(64.dp))
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "${current.temperatureCelsius.toInt()}°C",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(current.condition.label(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text(
                text = "Wind: ${current.windSpeedKmh} km/h",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun HourlyCell(hour: HourlyWeather) {
    val zone = TimeZone.currentSystemDefault()
    val hh = hour.time.toLocalDateTime(zone).hour
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("${hh}:00", style = MaterialTheme.typography.labelMedium)
        WeatherIcon(hour.condition, modifier = Modifier.size(32.dp).padding(vertical = 4.dp))
        Text("${hour.temperatureCelsius.toInt()}°", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DailyRow(daily: DailyWeather) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = daily.date.dayOfWeek.name.take(3),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(56.dp),
        )
        WeatherIcon(daily.condition, modifier = Modifier.size(32.dp))
        Text(
            text = daily.condition.label(),
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "${daily.minTemperatureCelsius.toInt()}° / ${daily.maxTemperatureCelsius.toInt()}°",
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
    }
}
