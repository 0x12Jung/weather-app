# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project snapshot

A 3-screen Android weather app across 5 Gradle modules (Compose + Hilt + Retrofit + typed DataStore). Tests are JVM-only — there is no instrumentation / Compose UI test suite. See `README.md` for the product-level overview, screen descriptions, and the rationale behind each tech choice.

## Build & test commands

```bash
./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:installDebug           # install on the connected device/emulator
./gradlew test                        # all JVM unit tests, every module
```

Per-module tests (note the task name differs — `:core:domain` is a pure-JVM module):

```bash
./gradlew :core:domain:test
./gradlew :core:data:testDebugUnitTest
./gradlew :core:ui:testDebugUnitTest
./gradlew :feature:weather:testDebugUnitTest
```

Single class or pattern:

```bash
./gradlew :core:domain:test --tests com.opnt.takehometest.core.domain.usecase.GetForecastUseCaseTest
./gradlew :core:domain:test --tests '*GetForecast*'
```

- No lint / ktlint / detekt / spotless is configured — do not invent a lint command.
- `androidTest` source sets are intentionally empty; do not propose instrumentation tests.

## Module layout and dependency rule

| Module | Plugin | Responsibility | May depend on |
|---|---|---|---|
| `:app` | `android.application` + Hilt | `MainActivity`, `Application`, root `AppNavHost`, Hilt aggregation | all other modules |
| `:core:domain` | `kotlin-jvm` only | Domain models, repository interfaces, use cases | nothing (pure JVM) |
| `:core:data` | `android.library` + Hilt | Retrofit APIs, DTOs, mappers, DataStore serializer, repo impls, DI modules | `:core:domain` |
| `:core:ui` | `android.library` + Compose | Theme, Material 3 palette, shared composables, Meteocons VDs | `:core:domain` |
| `:feature:weather` | `android.library` + Compose + Hilt | All 3 screens, ViewModels, sub-nav graph | `:core:domain`, `:core:ui` |

The one rule agents break first: `:feature:weather` **must not** depend on `:core:data`. Feature code sees only interfaces from `:core:domain`; concrete `Retrofit` / `DataStore` wiring stays behind Hilt modules in `:core:data`, aggregated by `:app`.

## Architecture conventions

- **Pattern** — MVVM with `StateFlow<UiState>` where `UiState` is a sealed interface per screen (e.g. `WeatherUiState.{Loading, NoCity, Success, Error}`). No MVI reducer.
- **DI** — Hilt via **KSP only, no KAPT**. Two Retrofit instances are disambiguated by the `@ForecastRetrofit` / `@GeocodingRetrofit` qualifiers defined in `core/data/.../di/Qualifiers.kt`.
- **Navigation** — Single-activity. `androidx.navigation.compose` 2.8 with type-safe `@Serializable` route objects (`feature/weather/.../navigation/WeatherRoutes.kt`). Feature modules expose `NavGraphBuilder.weatherGraph()` extensions; `:app` composes them inside `AppNavHost`.
- **ViewModel data flow** — observe selected city → `flatMapLatest` refresh trigger → invoke use case → emit Loading / Success / Error. See `WeatherViewModel`, `CityListViewModel`, `AddCityViewModel` for the canonical shape.
- **Error handling**
  - Catch `Exception`, never `Throwable`.
  - Use `Flow.catch { }` inside flow pipelines instead of wrapping collectors in `try/catch`.
  - Repositories let `IOException` / `HttpException` propagate; ViewModels map them to screen-specific sealed error enums (`WeatherError`, `AddCityError`). There is no `Result<T>` wrapper.
- **One-shot UI events** — model as fields on `UiState` with an `onXxxShown()` callback that the screen invokes after consuming. Do **not** introduce `SharedFlow` or `Channel` for new events.
- **Persistence** — typed `DataStore<SavedCitiesState>` with a custom `Serializer<SavedCitiesState>` (kotlinx.serialization JSON). Single file: `saved_cities.json`. No SharedPreferences, no Room.
- **Images / icons** — static Vector Drawables only (Meteocons, MIT). Coil / Glide are an intentional non-goal; do not add them.
- **Theming** — dark color scheme only; there is no light theme.

## Testing conventions

- **JUnit 4** (not 5), **MockK** (not Mockito), **Turbine** for `Flow` / `StateFlow`, **Google Truth** for assertions, `kotlinx.coroutines.test.runTest` for async.
- Retrofit integration tests use **MockWebServer** and load JSON fixtures from `core/data/src/test/resources/fixtures/`.
- Test doubles: **fakes** for `DataStore` (`FakeDataStore<T>`), **MockK mocks** for repositories / APIs / use cases.

## Version catalog

All versions live in `gradle/libs.versions.toml`. Module `build.gradle.kts` files reference entries via `libs.*` and `alias(libs.plugins.*)`. Add new dependencies by editing the TOML, not by hard-coding versions in module scripts.

## Key entry points

- `app/src/main/kotlin/com/opnt/takehometest/MainActivity.kt`
- `app/src/main/kotlin/com/opnt/takehometest/TakeHomeTestApplication.kt` — seeds Taipei on first launch via `SeedDefaultCityUseCase`
- `app/src/main/kotlin/com/opnt/takehometest/AppNavHost.kt`
- `feature/weather/.../navigation/WeatherGraph.kt` + `WeatherRoutes.kt`
- `core/data/.../di/{NetworkModule,DataStoreModule,RepositoryModule,Qualifiers}.kt`
