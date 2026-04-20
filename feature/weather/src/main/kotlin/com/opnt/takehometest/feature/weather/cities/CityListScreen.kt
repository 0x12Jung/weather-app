package com.opnt.takehometest.feature.weather.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opnt.takehometest.core.ui.component.EmptyView
import com.opnt.takehometest.core.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    onAddCity: () -> Unit,
    onBack: () -> Unit,
    onCitySelected: () -> Unit,
    viewModel: CityListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onAddCity) {
                        Text("Add city")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                CityListUiState.Loading -> LoadingIndicator()
                is CityListUiState.Content -> {
                    if (state.cities.isEmpty()) {
                        EmptyView(
                            message = "No saved cities",
                            actionLabel = "Add city",
                            onAction = onAddCity,
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(state.cities, key = { it.city.id }) { item ->
                                SwipeableCityRow(
                                    item = item,
                                    onTap = {
                                        viewModel.onSelectCity(item.city.id)
                                        onCitySelected()
                                    },
                                    onDismiss = { viewModel.onSwipeRemove(item.city) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCityRow(
    item: CityListItem,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.35f },
        confirmValueChange = {
            val shouldDismiss = it == SwipeToDismissBoxValue.EndToStart
            if (shouldDismiss) onDismiss()
            shouldDismiss
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    item.city.name,
                    fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            },
            supportingContent = {
                val subtitle = listOfNotNull(item.city.admin, item.city.country)
                    .joinToString(", ")
                Text(subtitle)
            },
            colors = if (item.isSelected) {
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            } else ListItemDefaults.colors(),
            modifier = Modifier.fillMaxWidth().clickable { onTap() },
        )
    }
}
