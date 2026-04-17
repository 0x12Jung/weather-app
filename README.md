# Weather Forecast — OpenNet Android Take-Home

A 3-screen Android app displaying the current weather and 7-day forecast for user-curated cities. Built as an interview take-home to demonstrate modern (2026) Android engineering judgement.

## Quick start

1. Clone the repository.
2. Open in Android Studio Hedgehog or newer.
3. Let Gradle sync (no API keys, no env vars).
4. Run `:app` on any API 29+ emulator/device.

Taipei is seeded on first launch so you see a populated screen immediately.

## Screens

- **Weather** — current temperature/condition/wind + next 24 hours + 7-day daily forecast.
- **Cities** — saved cities with swipe-to-remove + Undo; tap to switch.
- **Add city** — autocomplete search via Open-Meteo geocoding.

## Tech choices

| Layer | Choice | Rationale |
|-------|--------|-----------|
| Language/Build | Kotlin 2.3.10 + AGP 9.1.1 | Latest stable as of 2026-04-16. |
| UI | Jetpack Compose + Material 3 | Standard 2026 Android stack. |
| Architecture | MVVM + sealed `UiState` + events | Single source of truth without MVI reducer boilerplate for 3 screens. |
| DI | Hilt (KSP) | De-facto standard; zero-config for small apps. |
| API | [Open-Meteo](https://open-meteo.com) | **No API key required**, satisfying the "clone-and-run" constraint. PDF mentioned OpenWeatherMap as an example but it needs key setup. |
| Network | Retrofit 2 + OkHttp + kotlinx.serialization | Official Kotlin serialization, no KAPT, clean `@Serializable` annotations. |
| Persistence | Typed DataStore with `Serializer<SavedCitiesState>` | Structured list → typed store beats JSON-in-Preferences. |
| Navigation | `androidx.navigation.compose` 2.8 with type-safe `@Serializable` routes | Nav 3's advantages (multi-backstack, shared elements) are unused at 3 destinations. |
| Icons | Bundled Vector Drawables (Meteocons MIT) | 8–9 static icons; no Coil/Glide. Zero CDN dependency, flicker-free, offline-safe. |
| Testing | JUnit 4, MockK, Turbine, kotlinx-coroutines-test, Google Truth, MockWebServer | Pure-JVM tests — no instrumentation needed. |

## Modules

```
:app
:core:domain          (pure JVM — models, repo interfaces, use cases)
:core:data            (Retrofit + DataStore + implementations)
:core:ui              (theme + shared components + weather icon VDs)
:feature:weather      (WeatherScreen + CityListScreen + AddCityScreen)
```

`:feature:weather` depends on `:core:ui` and `:core:domain` only — UI layer cannot see Retrofit or DataStore. `:app` wires Hilt aggregation.

## Running tests

```bash
./gradlew test                    # all unit tests (JVM)
./gradlew :core:domain:test       # use cases
./gradlew :core:data:testDebugUnitTest      # repos, mappers, serializer, API
./gradlew :feature:weather:testDebugUnitTest # ViewModels
```

## Known limitations / non-goals

- No offline cache for weather data (intentional: stale forecasts mislead users).
- No GPS/device location.
- Celsius only.
- No UI instrumentation tests (unit tests cover ViewModels; Compose previews available in dev).
- English only.

## Attribution

Weather icons derived from [Meteocons](https://bas.dev/work/meteocons) (MIT). See `LICENSES/METEOCONS.md`.

## AI tools

See `AI_TOOLS.md`.
