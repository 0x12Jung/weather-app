# Weather Forecast App — Design Spec

**Date:** 2026-04-16
**Author:** Jung (with Claude)
**Status:** Draft for review
**Source assignment:** `Home_Assignment_-_ON_Android_Engineer_-_The_weather_forecast_app.pdf` (OpenNet Android role)

---

## 1. Overview

Android application displaying weather forecasts (today + weekly) for user-curated cities. The user can search and add cities via geocoding autocomplete, swipe to remove them, and switch between saved cities. The app is a take-home interview deliverable: 100% executable without API-key configuration, submitted via public GitHub.

### Goals

1. Demonstrate Kotlin + modern Android idioms (Compose, Coroutines, Flow).
2. Demonstrate Clean Architecture layering across modules.
3. Deliver all three required features in working condition.
4. Ship with a test suite (unit + integration) that runs in pure JVM.
5. Submission runs on clone → Open in Android Studio → Run. No API key, no extra setup.

### Non-goals

- Production-grade polish (animations, advanced Compose layouts).
- i18n / localization (English only).
- GPS / device-location detection.
- Celsius / Fahrenheit toggle (Celsius only).
- Offline weather data cache (weather is time-sensitive; stale cache misleads users).
- Multi-device / tablet / foldable adaptive layouts.

---

## 2. Assignment Requirements (verbatim)

The app **must**:

1. Display the weather forecast for the current day.
2. Display the weather forecast for the week.
3. Provide a city list where users can select a city to view its forecast.

Stack constraints (PDF-enforced):

- Kotlin
- Coroutines
- Jetpack Compose
- Clean Architecture approach
- At least one feature-module (multi-module / modularization)

Free choices (PDF):

- API provider (OpenWeatherMap mentioned as example)
- UI / visual design
- All library selections

---

## 3. Architecture

### 3.1 Pattern: MVVM with sealed UiState + event functions

Each screen has a `ViewModel` exposing a single `StateFlow<SomeUiState>` where `SomeUiState` is a sealed interface modeling `Loading / Success / Error / Empty`. User interactions go through explicit ViewModel event methods (e.g., `fun onCityClicked(city: City)`). This is the "Google NowInAndroid" style — unidirectional data flow with single source of truth, without introducing a Redux-style reducer framework.

**Why not full MVI (Orbit / custom reducer):** 3-screen scope does not justify the reducer boilerplate. The sealed-UiState approach provides the same guarantees (immutable state, explicit transitions) at lower cost.

### 3.2 Clean Architecture layering

```
┌───────────────────────────────────────────────────┐
│                 UI layer                          │
│  Composable  →  ViewModel  →  UseCase (domain)    │
└────────────────────────────────┬──────────────────┘
                                 │ depends on abstractions
┌────────────────────────────────▼──────────────────┐
│              Domain layer (pure JVM)              │
│  UseCases, RepositoryInterfaces, DomainModels     │
└────────────────────────────────▲──────────────────┘
                                 │ implements
┌────────────────────────────────┴──────────────────┐
│                 Data layer                        │
│  RepositoryImpl, Retrofit, DataStore, DTOs, Mappers│
└───────────────────────────────────────────────────┘
```

- UI and Data both depend on Domain (dependency inversion).
- Domain is framework-independent (pure Kotlin/JVM).
- Data implements interfaces declared in Domain.
- UI only knows UseCase invocations, never knows Retrofit or DataStore exists.

### 3.3 Module structure (4 modules)

```
:app
  ├─ TakeHomeTestApplication (@HiltAndroidApp)
  ├─ MainActivity (setContent → AppNavHost)
  ├─ AppNavHost (top-level NavHost)
  └─ build.gradle.kts

:core:domain                 (kotlin("jvm") — NOT an Android module)
  ├─ model/                  pure data classes
  ├─ repository/             interfaces only
  └─ usecase/                single-responsibility classes
  └─ build.gradle.kts        depends on: kotlinx-coroutines-core, kotlinx-serialization (optional)

:core:data                   (com.android.library)
  ├─ network/                Retrofit + DTOs + OpenMeteo APIs
  ├─ local/                  DataStore + Serializers
  ├─ repository/             RepositoryImpl classes
  ├─ mapper/                 DTO → domain conversion
  └─ di/                     @Module @InstallIn(SingletonComponent) Hilt bindings

:core:ui                     (com.android.library + compose)
  ├─ theme/                  Color, Typography, Shapes, AppTheme composable
  ├─ components/             LoadingIndicator, ErrorView, EmptyView, WeatherIcon
  └─ res/drawable/           ic_weather_clear, _cloudy, _rain, _snow, ... (8 VD XMLs)

:feature:weather             (com.android.library + compose)
  ├─ weather/                WeatherScreen + ViewModel + UiState + events
  ├─ cities/                 CityListScreen + ViewModel + UiState + events
  ├─ addcity/                AddCityScreen + ViewModel + UiState + events
  ├─ navigation/             @Serializable route classes + NavGraph extension fn
  └─ di/                     ViewModel-scoped bindings if needed
```

**Dependency graph:**

```
              :app
               │
        :feature:weather
         /    │    \
   :core:ui   │   :core:domain   ←── pure JVM
              │          ▲
              └──────────┘
                 :core:data  ←── implements :core:domain interfaces
```

- `:app` depends on `:feature:weather`, `:core:data` (for DI binding), `:core:ui`, `:core:domain`.
- `:feature:weather` depends on `:core:domain`, `:core:ui`. **NOT** on `:core:data` (so UI layer cannot import Retrofit etc.).
- `:core:data` depends on `:core:domain`.
- `:core:ui` is UI primitives only, no business logic.

---

## 4. Tech Stack

| Category | Library | Version (as of 2026-04-16) | Notes |
|----------|---------|---------------------------|-------|
| Language | Kotlin | **2.3.20** (latest stable) | Upgrade from scaffold 2.2.10 |
| Build | Android Gradle Plugin | 9.1.1 (existing) | Compatible with Kotlin 2.3.20 |
| Min/Target SDK | min 29 / target 36 | from scaffold | |
| Concurrency | kotlinx-coroutines | latest stable | `-core` in `:core:domain`, `-android` elsewhere |
| UI | Jetpack Compose + Material 3 | Compose BOM latest | Via `androidx.compose:compose-bom` |
| DI | Hilt | latest stable (2.5x+) | `hilt-android`, `hilt-compiler` (KSP), `hilt-navigation-compose` |
| Network | Retrofit | 2.11+ | |
| HTTP client | OkHttp | 4.12+ | + `logging-interceptor` |
| Serialization | kotlinx.serialization | 1.7+ | `json` artifact + `retrofit2-kotlinx-serialization-converter` |
| Persistence | androidx.datastore | latest | **Typed DataStore** (not Preferences) with custom kotlinx.serialization `Serializer<T>` |
| Navigation | androidx.navigation.compose | 2.8+ | Type-safe routes with `@Serializable` |
| Image/icon | Bundled Vector Drawable | — | Meteocons (MIT) → Android Studio Vector Asset Studio → VD XML. **No Coil.** |
| Unit test | JUnit | 4.13.2 (existing) | |
| Mocking | MockK | latest | Kotlin-first mocking |
| Flow testing | Turbine | latest | Cash App |
| Coroutine testing | kotlinx-coroutines-test | latest | `TestDispatcher`, `runTest` |
| Assertions | Google Truth | latest | More readable than JUnit assert |
| HTTP mocking | MockWebServer | bundled with OkHttp | For Retrofit integration tests |

**Why kotlinx.serialization over Moshi/Gson:** official Kotlin support, no KAPT, faster, cleaner `@Serializable` annotations.

**Why Typed DataStore over Preferences DataStore:** the saved-cities value is a structured list; Typed DataStore with a kotlinx.serialization `Serializer<T>` gives compile-time safety and schema-evolution support without ad-hoc JSON-in-String hacks.

**Why bundled Vector Drawables (no Coil):** weather icons must be offline-available, flicker-free on cold start, and zero CDN dependency. Any image library for 8 static icons is over-engineering. Decision rationale will be documented in README.

**Why Compose Navigation 2.x (not Nav 3):** Nav 3's real advantages (multi-backstack, shared-element transitions, state-hoisted backstack) are unused in a 3-destination linear flow. Nav 2 with type-safe routes is idiomatic 2026. README will acknowledge Nav 3 exists and justify the trade-off.

---

## 5. API: Open-Meteo

### 5.1 Why Open-Meteo over OpenWeatherMap

- **No API key required** → submission is 100% executable on clone (a hard requirement from the PDF).
- Modern JSON schema with named fields (easier to parse than OWM's nested IDs).
- Free geocoding endpoint (same provider, consistent coverage).
- 7-day forecast included free.
- Generous rate limits (10k req/day free).

### 5.2 Endpoints used

**Forecast** (`GET https://api.open-meteo.com/v1/forecast`)

Required query parameters:
```
latitude=25.0330
longitude=121.5654
current=temperature_2m,weather_code,wind_speed_10m,is_day
hourly=temperature_2m,weather_code
daily=weather_code,temperature_2m_max,temperature_2m_min
timezone=auto
forecast_days=7
```

Response schema (relevant subset):
```json
{
  "latitude": 25.03,
  "longitude": 121.57,
  "timezone": "Asia/Taipei",
  "current": {
    "time": "2026-04-16T14:00",
    "temperature_2m": 22.5,
    "weather_code": 2,
    "wind_speed_10m": 3.4,
    "is_day": 1
  },
  "hourly": {
    "time": ["2026-04-16T14:00", "2026-04-16T15:00", ...],
    "temperature_2m": [22.5, 22.8, ...],
    "weather_code": [2, 2, 3, ...]
  },
  "daily": {
    "time": ["2026-04-16", "2026-04-17", ...],
    "weather_code": [2, 61, 61, 3, 0, 0, 1],
    "temperature_2m_max": [25.1, 22.0, ...],
    "temperature_2m_min": [18.2, 17.5, ...]
  }
}
```

Only hourly entries where `time ≥ now` are shown (next 24 h rolling).

**Geocoding** (`GET https://geocoding-api.open-meteo.com/v1/search`)

```
name=Taipei
count=10
language=en
format=json
```

Response (relevant):
```json
{
  "results": [
    {
      "id": 1668341,
      "name": "Taipei",
      "latitude": 25.04,
      "longitude": 121.56,
      "country": "Taiwan",
      "admin1": "Taipei City",
      "country_code": "TW"
    }
  ]
}
```

`results` is absent when no match — must handle as empty list, not null crash.

### 5.3 WMO weather_code → `WeatherCondition` mapping

28 codes grouped into 8 visual buckets:

| Bucket | WMO codes | Drawable |
|--------|-----------|----------|
| Clear | 0, 1 | `ic_weather_clear` |
| PartlyCloudy | 2 | `ic_weather_partly_cloudy` |
| Cloudy | 3 | `ic_weather_cloudy` |
| Fog | 45, 48 | `ic_weather_fog` |
| Drizzle | 51, 53, 55, 56, 57 | `ic_weather_drizzle` |
| Rain | 61, 63, 65, 66, 67, 80, 81, 82 | `ic_weather_rain` |
| Snow | 71, 73, 75, 77, 85, 86 | `ic_weather_snow` |
| Thunderstorm | 95, 96, 99 | `ic_weather_thunder` |

Fallback for unknown/future codes: `Cloudy`.

---

## 6. Domain Layer (`:core:domain`, pure JVM)

### 6.1 Domain models

```kotlin
// model/Coordinates.kt
data class Coordinates(val latitude: Double, val longitude: Double)

// model/City.kt
data class City(
    val id: Long,             // from Open-Meteo geocoding result
    val name: String,
    val country: String,
    val admin: String?,       // e.g., "Taipei City"
    val coordinates: Coordinates,
    val timezone: String      // e.g., "Asia/Taipei"
)

// model/WeatherCondition.kt
enum class WeatherCondition {
    Clear, PartlyCloudy, Cloudy, Fog, Drizzle, Rain, Snow, Thunderstorm;
    companion object { fun fromWmoCode(code: Int): WeatherCondition = ... }
}

// model/Forecast.kt
data class Forecast(
    val fetchedAt: Instant,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,   // next 24 entries
    val daily: List<DailyWeather>      // 7 entries
)

data class CurrentWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val windSpeedKmh: Double,
    val isDay: Boolean
)

data class HourlyWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition
)

data class DailyWeather(
    val date: LocalDate,
    val condition: WeatherCondition,
    val maxTemperatureCelsius: Double,
    val minTemperatureCelsius: Double
)
```

Uses `kotlinx-datetime` (`Instant`, `LocalDate`) for multiplatform-friendly types. (`java.time` works too, but kotlinx-datetime is more idiomatic in pure-JVM Kotlin modules.)

### 6.2 Repository interfaces

```kotlin
// repository/WeatherRepository.kt
interface WeatherRepository {
    suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast
}

// repository/CityRepository.kt
interface CityRepository {
    fun observeSavedCities(): Flow<List<City>>
    fun observeSelectedCity(): Flow<City?>
    suspend fun searchCities(query: String): List<City>
    suspend fun addCity(city: City)
    suspend fun removeCity(cityId: Long)
    suspend fun setSelectedCity(cityId: Long)
}
```

Rationale:
- `searchCities` is `suspend` (one-shot request, no caching).
- `getForecast` is `suspend` (one-shot, no caching per non-goal).
- `observeSavedCities` / `observeSelectedCity` are `Flow` — UI observes persistence changes.

### 6.3 Use cases

Each is a class with a single public `operator fun invoke` (or suspend variant):

| Use case | Signature | Used by |
|----------|-----------|---------|
| `GetForecastUseCase` | `suspend operator fun invoke(city: City): Forecast` | WeatherViewModel |
| `ObserveSavedCitiesUseCase` | `operator fun invoke(): Flow<List<City>>` | CityListViewModel |
| `ObserveSelectedCityUseCase` | `operator fun invoke(): Flow<City?>` | WeatherViewModel |
| `SearchCitiesUseCase` | `suspend operator fun invoke(query: String): List<City>` | AddCityViewModel |
| `AddCityUseCase` | `suspend operator fun invoke(city: City)` | AddCityViewModel |
| `RemoveCityUseCase` | `suspend operator fun invoke(cityId: Long)` | CityListViewModel |
| `SelectCityUseCase` | `suspend operator fun invoke(cityId: Long)` | CityListViewModel |
| `SeedDefaultCityUseCase` | `suspend operator fun invoke()` | Application / splash |

**Use case behavior notes:**

- `SearchCitiesUseCase`: trims the query; returns empty list if trimmed length < 2 (avoids noisy one-character requests). Otherwise delegates to `cityRepo.searchCities(trimmed)`.
- `SeedDefaultCityUseCase`: on first launch (saved cities empty), inserts Taipei (hardcoded constants: `id=1668341, lat=25.0330, lon=121.5654, country="Taiwan", timezone="Asia/Taipei"`) and sets it as selected city. Idempotent — checks existing list before seeding; does nothing if list is non-empty.
- `AddCityUseCase` / `RemoveCityUseCase` / `SelectCityUseCase`: thin wrappers that delegate to the repository. Included as domain-layer entry points so ViewModels never see repository interfaces directly.

---

## 7. Data Layer (`:core:data`)

### 7.1 Network

**Retrofit service interfaces:**

```kotlin
interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,is_day",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int = 7
    ): ForecastResponseDto
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponseDto
}
```

Two separate Retrofit clients because the two APIs have **different base URLs**:
- `https://api.open-meteo.com/`
- `https://geocoding-api.open-meteo.com/`

Hilt provides them as separately-qualified `@Provides` with qualifier annotations.

**OkHttp configuration:**

```kotlin
@Provides @Singleton
fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
    })
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()
```

**JSON configuration:**

```kotlin
@Provides @Singleton
fun provideJson(): Json = Json {
    ignoreUnknownKeys = true    // forward-compat if Open-Meteo adds fields
    explicitNulls = false
    isLenient = true
}
```

### 7.2 DTOs

Mirror Open-Meteo's JSON exactly (snake_case via `@SerialName`):

```kotlin
@Serializable
data class ForecastResponseDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto
) {
    @Serializable
    data class CurrentDto(
        val time: String,
        @SerialName("temperature_2m") val temperature2m: Double,
        @SerialName("weather_code") val weatherCode: Int,
        @SerialName("wind_speed_10m") val windSpeed10m: Double,
        @SerialName("is_day") val isDay: Int
    )
    @Serializable
    data class HourlyDto(
        val time: List<String>,
        @SerialName("temperature_2m") val temperature2m: List<Double>,
        @SerialName("weather_code") val weatherCode: List<Int>
    )
    @Serializable
    data class DailyDto(
        val time: List<String>,
        @SerialName("weather_code") val weatherCode: List<Int>,
        @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
        @SerialName("temperature_2m_min") val temperatureMin: List<Double>
    )
}

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList()
) {
    @Serializable
    data class GeocodingResultDto(
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val country: String,
        val admin1: String? = null,
        val timezone: String
    )
}
```

### 7.3 Mappers

DTO → domain conversion in `mapper/ForecastMapper.kt`, `mapper/CityMapper.kt`. Pure functions, easily tested.

**`ForecastMapper.toDomain(dto, fetchedAt: Instant): Forecast`** — takes a `fetchedAt` parameter (injected by repository using `Clock` or `Instant.now()`); stamps it on the domain `Forecast.fetchedAt` and uses it as the reference "now" for filtering hourly entries:

1. Zip the three parallel hourly arrays (`time`, `temperature_2m`, `weather_code`) into `List<HourlyWeather>`.
2. Parse each `time` string as `LocalDateTime`, combine with response `timezone` to produce `Instant`.
3. `.filter { it.time >= fetchedAt }.take(24)` — next 24 hours only.

Injecting `fetchedAt` instead of calling `Instant.now()` inside the mapper keeps it a pure function (easy to test with deterministic timestamps).

**`CityMapper`** — converts `GeocodingResultDto` ↔ domain `City` ↔ `SerializableCity`. Three directions since the DataStore's internal `SerializableCity` is separate from the domain `City` (to isolate persistence schema from domain).

### 7.4 Persistence — Typed DataStore

**Internal (serialized) models:**

```kotlin
@Serializable
internal data class SavedCitiesState(
    val cities: List<SerializableCity> = emptyList(),
    val selectedCityId: Long? = null
)

@Serializable
internal data class SerializableCity(
    val id: Long,
    val name: String,
    val country: String,
    val admin: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)
```

**Serializer:**

```kotlin
internal object SavedCitiesSerializer : Serializer<SavedCitiesState> {
    override val defaultValue = SavedCitiesState()
    override suspend fun readFrom(input: InputStream): SavedCitiesState =
        try { Json.decodeFromStream(input) } catch (e: SerializationException) {
            throw CorruptionException("Cannot read SavedCitiesState", e)
        }
    override suspend fun writeTo(t: SavedCitiesState, output: OutputStream) =
        Json.encodeToStream(t, output)
}
```

**DataStore provision:**

```kotlin
@Provides @Singleton
fun provideSavedCitiesDataStore(@ApplicationContext context: Context): DataStore<SavedCitiesState> =
    DataStoreFactory.create(
        serializer = SavedCitiesSerializer,
        produceFile = { context.dataStoreFile("saved_cities.json") }
    )
```

### 7.5 Repository implementations

```kotlin
internal class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoForecastApi,
    private val mapper: ForecastMapper
) : WeatherRepository {
    override suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast =
        mapper.toDomain(api.getForecast(coordinates.latitude, coordinates.longitude, timezone = timezone))
}

internal class CityRepositoryImpl @Inject constructor(
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val dataStore: DataStore<SavedCitiesState>,
    private val mapper: CityMapper
) : CityRepository {

    override fun observeSavedCities(): Flow<List<City>> =
        dataStore.data.map { state -> state.cities.map { mapper.toDomain(it) } }

    override fun observeSelectedCity(): Flow<City?> =
        dataStore.data.map { state ->
            state.cities.firstOrNull { it.id == state.selectedCityId }?.let(mapper::toDomain)
        }

    override suspend fun searchCities(query: String): List<City> =
        geocodingApi.search(name = query.trim())
            .results.map(mapper::toDomain)

    override suspend fun addCity(city: City) {
        dataStore.updateData { state ->
            if (state.cities.any { it.id == city.id }) state
            else state.copy(
                cities = state.cities + mapper.toSerializable(city),
                selectedCityId = state.selectedCityId ?: city.id   // auto-select first added
            )
        }
    }

    override suspend fun removeCity(cityId: Long) {
        dataStore.updateData { state ->
            val remaining = state.cities.filterNot { it.id == cityId }
            val newSelected = when {
                state.selectedCityId != cityId -> state.selectedCityId
                remaining.isNotEmpty() -> remaining.first().id
                else -> null
            }
            state.copy(cities = remaining, selectedCityId = newSelected)
        }
    }

    override suspend fun setSelectedCity(cityId: Long) {
        dataStore.updateData { it.copy(selectedCityId = cityId) }
    }
}
```

Hilt bindings via `@Binds` in `DataModule`.

---

## 8. UI Layer (`:feature:weather`)

### 8.1 Navigation

Type-safe routes with `@Serializable` data objects/classes:

```kotlin
@Serializable data object WeatherRoute
@Serializable data object CityListRoute
@Serializable data object AddCityRoute
```

NavGraph extension:

```kotlin
fun NavGraphBuilder.weatherGraph(navController: NavController) {
    composable<WeatherRoute> { WeatherScreen(onOpenCityList = { navController.navigate(CityListRoute) }) }
    composable<CityListRoute> { CityListScreen(
        onAddCity = { navController.navigate(AddCityRoute) },
        onBack = { navController.popBackStack() }
    ) }
    composable<AddCityRoute> { AddCityScreen(onCityAdded = { navController.popBackStack() }) }
}
```

**Start destination:** `WeatherRoute`.

### 8.2 Screen: WeatherScreen

**UiState:**
```kotlin
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object NoCity : WeatherUiState                    // saved cities = empty
    data class Success(
        val city: City,
        val forecast: Forecast,
        val isRefreshing: Boolean = false
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

**Flow:**
1. ViewModel collects `observeSelectedCityUseCase()` → when a city is emitted, launches `getForecastUseCase(city)`.
2. If selected city is null (no saved cities), state is `NoCity` (user taps CTA → AddCityRoute).
3. Pull-to-refresh triggers `onRefresh()` → sets `isRefreshing = true`, re-fetches, clears on completion.

**UI layout (top-to-bottom):**
- TopAppBar: city name + country, trailing IconButton (List icon) → `onOpenCityList`.
- Current weather card: large weather icon, temperature, condition text, wind speed, "Last updated HH:mm".
- Hourly section (horizontal LazyRow, 24 entries): small icon + hour + temperature.
- Daily section (LazyColumn, 7 entries): day-of-week label + min/max + icon.
- PullToRefreshBox wrapping the scroll content.

**Error state:** centered ErrorView with retry button → invokes ViewModel `onRetry()`.

### 8.3 Screen: CityListScreen

**UiState:**
```kotlin
sealed interface CityListUiState {
    data object Loading : CityListUiState
    data class Content(
        val cities: List<CityListItem>,
        val selectedCityId: Long?
    ) : CityListUiState
}

data class CityListItem(val city: City, val isSelected: Boolean)
```

**Flow:**
1. ViewModel collects `observeSavedCitiesUseCase()` and `observeSelectedCityUseCase()`, combines into Content.
2. Tapping a row → `selectCityUseCase(id)` → pops back to WeatherRoute.
3. Swipe-to-dismiss a row → `removeCityUseCase(id)` + show Undo Snackbar (7-second window).
4. FAB "+" → `navController.navigate(AddCityRoute)`.

**UI:**
- TopAppBar: "Cities" title + back navigation.
- LazyColumn of cities. Each item in a `SwipeToDismissBox` (Material 3 stable API).
- Empty state: "No saved cities. Tap + to add a city." (shouldn't happen often since Taipei is seeded, but handle it).
- FAB (bottom end).
- SnackbarHost for undo.

**Undo implementation:** on swipe, optimistically remove from UI; ViewModel caches the removed city; show Snackbar with "Undo" action; if pressed within 7 seconds, re-insert via `addCityUseCase`.

### 8.4 Screen: AddCityScreen

**UiState:**
```kotlin
sealed interface AddCityUiState {
    data class Idle(val query: String = "") : AddCityUiState
    data class Loading(val query: String) : AddCityUiState
    data class Results(val query: String, val cities: List<AddCityResultItem>) : AddCityUiState
    data class NoResults(val query: String) : AddCityUiState
    data class Error(val query: String, val message: String) : AddCityUiState
}

data class AddCityResultItem(val city: City, val alreadySaved: Boolean)
```

**Flow:**
1. Search text field drives a `MutableStateFlow<String>`.
2. ViewModel observes the query with:
   ```kotlin
   query
     .debounce(300.milliseconds)
     .distinctUntilChanged()
     .mapLatest { q ->
         when {
             q.length < 2 -> AddCityUiState.Idle(q)
             else -> loadResults(q)  // Loading → Results/NoResults/Error
         }
     }
   ```
3. Each `loadResults` is `mapLatest` so previous in-flight search is cancelled on new keystroke.
4. Tapping a result → `addCityUseCase(city)` → `onCityAdded` callback (pops back).
5. If `alreadySaved`, row is visually disabled with "Already saved" label; tapping is a no-op.

**UI:**
- TopAppBar: search text field (focus on entry) + back nav.
- List of matches under it, each row shows `name, admin1, country` and (if saved) "Already saved".
- Loading indicator inline below search field.
- Empty / idle / error states each with clear message.

### 8.5 Shared components (`:core:ui`)

- `LoadingIndicator` — centered `CircularProgressIndicator`.
- `ErrorView(message, onRetry)` — icon + message + retry button.
- `EmptyView(message, action)` — icon + message + optional CTA button.
- `WeatherIcon(condition, modifier)` — looks up drawable by `condition`, wraps `Icon` or `Image`.
- `AppTheme` — Material 3 `dynamicColorScheme` on Android 12+, fallback palette below. Supports light/dark via `isSystemInDarkTheme()`.

---

## 9. Error Handling

### 9.1 Categorization

Data layer produces specific exceptions, mapped to user-facing messages in ViewModel:

| Exception | User message (English) |
|-----------|-----------------------|
| `IOException` / no network | "No internet connection. Check your network and try again." |
| `HttpException(4xx)` | "Request failed ({code}). Please try again." |
| `HttpException(5xx)` | "Weather service temporarily unavailable." |
| `SerializationException` | "Unexpected response from server." |
| `TimeoutException` | "Request timed out. Check your network." |
| Any other `Throwable` | "Something went wrong." (log full stack trace) |

### 9.2 No crashes policy

ViewModels use `runCatching` or explicit try/catch at use-case call sites; exceptions never bubble to Compose. Uncaught exceptions land in `Error` UiState with retry affordance.

### 9.3 Logging

Use `android.util.Log` in Data/UI layers. Domain (pure JVM) has no logging — caller logs. No third-party logger (Timber etc.) — unnecessary dependency.

---

## 10. Edge Cases

| Scenario | Behavior |
|----------|----------|
| First launch, no saved cities | `SeedDefaultCityUseCase` inserts Taipei and selects it → WeatherScreen shows Taipei immediately. |
| `SeedDefaultCityUseCase` runs but network is offline during initial fetch | WeatherScreen shows `Error` state with retry button. City list still contains Taipei. |
| User deletes the currently selected city | Repository auto-selects the first remaining city; if list becomes empty, `selectedCityId = null`, WeatherScreen shows `NoCity`. |
| User deletes the last city | `selectedCityId = null`, WeatherScreen shows `NoCity` with "Add a city" CTA. |
| User adds a city already in the list | Repository silently no-ops (idempotent). AddCity UI prevents tap via `alreadySaved` flag. |
| Geocoding returns empty results | `AddCityUiState.NoResults` with "No cities match '{query}'". |
| User types very fast / cancels search | `mapLatest` cancels in-flight search, latest query wins. |
| Pull-to-refresh while already refreshing | Debounced in ViewModel (`isRefreshing` flag checked first). |
| Undo window expires | Snackbar dismisses; removal is permanent. |
| Process death mid-search | `savedStateHandle` preserves query text and selected city; ViewModel re-initializes and re-observes Flows. |
| User backgrounds app then returns | Flows naturally re-emit latest DataStore state on resume. Weather is re-fetched only on pull-to-refresh or screen re-entry. |

---

## 11. Testing Strategy

### 11.1 Test layers and locations

| Module | Test type | Location | What's tested |
|--------|-----------|----------|---------------|
| `:core:domain` | Pure JVM unit | `src/test/kotlin/` | Use cases with MockK'd repositories; `WeatherCondition.fromWmoCode` mapping; `SeedDefaultCityUseCase` idempotency |
| `:core:data` | Pure JVM unit | `src/test/kotlin/` | Mappers (DTO → domain); Repository implementations with MockK'd API and fake `DataStore`; MockWebServer integration tests for Retrofit services |
| `:feature:weather` | Pure JVM unit | `src/test/kotlin/` | ViewModels with MockK'd use cases; StateFlow emission via Turbine; debounce + mapLatest search behavior |

### 11.2 Test fixtures

- `core/data/src/test/resources/fixtures/forecast_response_taipei.json` — realistic Open-Meteo forecast response.
- `core/data/src/test/resources/fixtures/geocoding_response_taipei.json` — geocoding result.
- `core/data/src/test/resources/fixtures/geocoding_response_empty.json` — no results case.

MockWebServer tests read fixtures from classpath and `enqueue` with proper Content-Type headers.

### 11.3 Key test scenarios (representative list; full list in implementation plan)

**Use cases**
- `GetForecastUseCase_fetches_and_returns_forecast`
- `SearchCitiesUseCase_trims_and_delegates_to_repo`
- `SearchCitiesUseCase_returns_empty_list_for_short_query`
- `SeedDefaultCityUseCase_is_noop_when_cities_already_exist`
- `RemoveCityUseCase_auto_selects_next_city_when_removing_selected`

**Repositories**
- `WeatherRepositoryImpl_maps_DTO_to_domain_correctly`
- `WeatherRepositoryImpl_filters_hourly_to_next_24_hours`
- `CityRepositoryImpl_addCity_is_idempotent_on_duplicate_id`
- `CityRepositoryImpl_removeCity_updates_selectedCityId_when_removing_selected`

**Retrofit / MockWebServer**
- `OpenMeteoForecastApi_parses_real_response_fixture_successfully`
- `OpenMeteoGeocodingApi_handles_empty_results_array`
- `OpenMeteoGeocodingApi_handles_missing_results_field` (response omits `results` key entirely)

**ViewModels**
- `WeatherViewModel_emits_Loading_then_Success_on_city_with_valid_forecast`
- `WeatherViewModel_emits_NoCity_when_no_city_selected`
- `WeatherViewModel_emits_Error_on_network_failure`
- `CityListViewModel_emits_combined_cities_and_selection`
- `AddCityViewModel_debounces_query_by_300ms`
- `AddCityViewModel_cancels_previous_search_on_new_query`

### 11.4 Out of scope for tests

- Compose UI tests (instrumentation) — optional stretch goal, not blocking delivery.
- End-to-end / Espresso — not needed.

---

## 12. Deliverables

- [ ] Public GitHub repository (not private).
- [ ] Clone → Android Studio "Open an existing project" → Gradle sync → Run. **Zero configuration, zero API keys.**
- [ ] `README.md` covering:
  - Build & run steps
  - Module structure explanation
  - Architecture decisions (MVVM + sealed UiState, Clean Architecture, module split, library choices, Nav 2 vs Nav 3 trade-off)
  - Testing how-to (`./gradlew test`)
  - Screenshots (from emulator)
- [ ] `AI_TOOLS.md` disclosing AI assistance (Claude Code) and how it was used.
- [ ] License file for Meteocons attribution (MIT, include original notice).
- [ ] All tests green on `./gradlew test`.

---

## 13. Implementation Sequencing (preview — full plan follows in `docs/superpowers/plans/`)

Rough ordering (actual tasks with TDD steps will be in the plan document):

1. Upgrade Kotlin to 2.3.20; sync scaffold.
2. Create module skeletons (`:core:domain`, `:core:data`, `:core:ui`, `:feature:weather`) with empty builds.
3. Domain layer: models, repository interfaces, use cases + tests.
4. Data layer: network DTOs, Retrofit APIs, MockWebServer tests for Open-Meteo responses.
5. Data layer: mappers + tests.
6. Data layer: Typed DataStore + Serializer + tests.
7. Data layer: Repository impls + tests; Hilt bindings.
8. UI layer (`:core:ui`): theme, shared components, weather icons (download + convert SVGs).
9. UI layer: WeatherScreen + ViewModel + tests.
10. UI layer: CityListScreen + ViewModel + tests (incl. swipe-to-dismiss + undo).
11. UI layer: AddCityScreen + ViewModel + tests (incl. debounce + mapLatest).
12. `:app` wiring: Navigation graph, Application class, MainActivity.
13. Seed default city wiring.
14. README + AI_TOOLS.md + screenshots.
15. Final `./gradlew test` + manual smoke test.

---

## 14. Open Questions / Risks

- **Compose BOM version compatibility with Kotlin 2.3.20**: Compose Compiler is versioned with Kotlin itself (via `org.jetbrains.kotlin.plugin.compose`). Compose BOM just aligns library versions. Should Just Work but verify after Kotlin bump.
- **Hilt KSP compatibility with Kotlin 2.3.20**: Hilt added KSP2 support in 2.5x. Will verify during plan execution; fall back to KAPT if KSP has issues.
- **Meteocons SVG-to-VectorDrawable conversion**: some Meteocons static icons may use features VD doesn't support (gradients with complex stops). Fallback: simplify in Illustrator / Inkscape, or use an alternate open-source icon set (Weather Icons by Erik Flowers as Plan B).
- **Network permission**: `android.permission.INTERNET` must be in `AndroidManifest.xml` — typically present by default, verify.
