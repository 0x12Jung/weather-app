package com.opnt.takehometest.feature.weather.addcity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opnt.takehometest.core.ui.component.EmptyView
import com.opnt.takehometest.core.ui.component.ErrorView
import com.opnt.takehometest.core.ui.component.LoadingIndicator
import com.opnt.takehometest.feature.weather.R
import com.opnt.takehometest.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityScreen(
    onCityAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddCityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    var queryInput by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val addFailedMessage = stringResource(R.string.add_city_error_add_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { evt ->
            when (evt) {
                AddCityEvent.CityAdded -> onCityAdded()
                AddCityEvent.AddFailed -> snackbarHostState.showSnackbar(addFailedMessage)
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_city_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.core_ui_cd_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = queryInput,
                    onValueChange = {
                        queryInput = it
                        viewModel.onQueryChange(it)
                    },
                    label = { Text(stringResource(R.string.add_city_search_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester),
                )

                when (val state = uiState) {
                    is AddCityUiState.Idle -> EmptyView(
                        message = stringResource(R.string.add_city_empty_idle),
                    )
                    is AddCityUiState.Loading -> LoadingIndicator()
                    is AddCityUiState.NoResults -> EmptyView(
                        message = stringResource(R.string.add_city_empty_no_results),
                    )
                    is AddCityUiState.Error -> {
                        val msg = when (state.error) {
                            AddCityError.SearchFailed ->
                                stringResource(R.string.add_city_error_search_failed)
                        }
                        ErrorView(msg)
                    }
                    is AddCityUiState.Results -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.cities, key = { it.city.id }) { item ->
                            ListItem(
                                headlineContent = { Text(item.city.name) },
                                supportingContent = {
                                    val subtitle = listOfNotNull(item.city.admin, item.city.country)
                                        .joinToString(", ")
                                    Text(subtitle)
                                },
                                trailingContent = if (item.alreadySaved) {
                                    @Composable {
                                        Text(
                                            stringResource(R.string.add_city_trailing_already_saved),
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                    }
                                } else null,
                                colors = ListItemDefaults.colors(),
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(enabled = !item.alreadySaved) {
                                        viewModel.onAddCity(item.city)
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}
