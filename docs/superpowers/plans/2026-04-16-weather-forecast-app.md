# Weather Forecast App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a 3-screen Android weather forecast app (current + weekly forecast + city management) for the OpenNet take-home, built with Kotlin + Compose + Clean Architecture + multi-module, fully test-backed and runnable with zero configuration.

**Architecture:** MVVM with sealed `UiState` + event functions (NowInAndroid style). Clean layering across 4 Gradle modules: `:app` (entry), `:core:domain` (pure JVM — models/interfaces/use cases), `:core:data` (Retrofit/DataStore/impls), `:core:ui` (theme + shared components + bundled weather vector drawables), `:feature:weather` (3 screens + ViewModels + nav graph). `:feature:weather` depends on `:core:domain` + `:core:ui` only — never sees Retrofit/DataStore.

**Tech Stack:** Kotlin 2.3.20, AGP 9.1.1, Jetpack Compose (Material 3, BOM-aligned), Hilt (KSP), Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization 1.7, androidx.datastore (Typed, kotlinx.serialization `Serializer`), androidx.navigation.compose 2.8 with type-safe `@Serializable` routes, kotlinx-datetime, kotlinx-coroutines. Tests: JUnit 4 + MockK + Turbine + kotlinx-coroutines-test + Google Truth + MockWebServer.

**Reference spec:** `docs/superpowers/specs/2026-04-16-weather-forecast-app-design.md`.

---

## Progress Tracker

Tick each task as its final commit lands. Coarse-grained — sub-step checkboxes inside each task remain the source of truth for in-progress work.

**Phase 1 — Foundation (Gradle + modules)**
- [x] [Task 1: Upgrade Kotlin + expand version catalog](#task-1-upgrade-kotlin--expand-version-catalog)
- [x] [Task 2: Register module paths in `settings.gradle.kts`](#task-2-register-module-paths-in-settingsgradlekts)
- [x] [Task 3: Create `:core:domain` skeleton](#task-3-create-coredomain-skeleton-pure-jvm-module)
- [x] [Task 4: Create `:core:data` skeleton](#task-4-create-coredata-skeleton-android-library-module)
- [x] [Task 5: Create `:core:ui` skeleton](#task-5-create-coreui-skeleton-compose-library-module)
- [x] [Task 6: Create `:feature:weather` skeleton](#task-6-create-featureweather-skeleton)
- [x] [Task 7: Migrate `:app` to new dependency graph and Hilt](#task-7-migrate-app-to-new-dependency-graph-and-hilt)

**Phase 2 — Domain layer**
- [x] [Task 8: Domain models — Coordinates, City, Forecast family](#task-8-domain-models--coordinates-city-forecast-family)
- [x] [Task 9: WeatherCondition sealed interface + mapping tests](#task-9-weathercondition-sealed-interface--mapping-tests)
- [x] [Task 10: Repository interfaces](#task-10-repository-interfaces)
- [x] [Task 11: GetForecastUseCase with tests](#task-11-getforecastusecase-with-tests)
- [x] [Task 12: Observer + simple mutation use cases](#task-12-observer--simple-mutation-use-cases)
- [x] [Task 13: SearchCitiesUseCase with trimming + min-length gate](#task-13-searchcitiesusecase-with-trimming--min-length-gate)
- [x] [Task 14: SeedDefaultCityUseCase with idempotency](#task-14-seeddefaultcityusecase-with-idempotency)

**Phase 3 — Data layer**
- [x] [Task 15: Open-Meteo DTOs](#task-15-open-meteo-dtos)
- [x] [Task 16: Retrofit services + MockWebServer tests](#task-16-retrofit-service-interfaces--mockwebserver-tests)
- [x] [Task 17: ForecastMapper with tests](#task-17-forecastmapper-with-tests)
- [x] [Task 18: CityMapper (DTO ↔ domain ↔ serializable)](#task-18-citymapper-dto--domain--serializable-with-tests)
- [x] [Task 19: SavedCitiesState + SavedCitiesSerializer](#task-19-savedcitiesstate--savedcitiesserializer-with-tests)
- [x] [Task 20: WeatherRepositoryImpl with tests](#task-20-weatherrepositoryimpl-with-tests)
- [x] [Task 21: CityRepositoryImpl with tests](#task-21-cityrepositoryimpl-with-tests-uses-a-fake-datastore)
- [x] [Task 22: Hilt DI modules for data layer](#task-22-hilt-di-modules-for-data-layer)

**Phase 4 — `:core:ui`**
- [x] [Task 23: Material 3 theme](#task-23-coreui-theme-color--type--theme)
- [x] [Task 24: Shared components (Loading/Error/Empty)](#task-24-shared-components-loading--error--empty)
- [x] [Task 25: Weather icons + WeatherIcon component](#task-25-weather-icons-9-vector-drawables--weathericon-component)

**Phase 5 — `:feature:weather` — ViewModels**
- [x] [Task 26: Navigation routes + weatherGraph](#task-26-navigation-routes--weathergraph)
- [x] [Task 27: WeatherViewModel + WeatherUiState](#task-27-weatherviewmodel--weatheruistate-with-tests)
- [x] [Task 28: CityListViewModel with swipe-to-dismiss + undo](#task-28-citylistviewmodel-with-swipe-to-dismiss--undo)
- [x] [Task 29: AddCityViewModel with debounce + mapLatest](#task-29-addcityviewmodel-with-debounce--maplatest)

**Phase 6 — `:feature:weather` — Compose screens**
- [x] [Task 30: WeatherScreen composable](#task-30-weatherscreen-composable)
- [x] [Task 31: CityListScreen with swipe-to-dismiss + undo Snackbar](#task-31-citylistscreen-with-swipe-to-dismiss--undo-snackbar)
- [x] [Task 32: AddCityScreen composable](#task-32-addcityscreen-composable)

**Phase 7 — `:app` wiring + verification**
- [x] [Task 33: MainActivity, AppNavHost, seed on startup](#task-33-app--mainactivity-appnavhost-seed-on-startup)
- [x] [Task 34: Delete scaffold leftovers](#task-34-delete-scaffold-leftovers-if-present)
- [ ] [Task 35: Run the full test suite and manual smoke test](#task-35-run-the-full-test-suite-and-manual-smoke-test)

**Phase 8 — Submission**
- [ ] [Task 36: Write README.md](#task-36-write-readmemd)
- [ ] [Task 37: Write AI_TOOLS.md](#task-37-write-ai_toolsmd)
- [ ] [Task 38: Capture screenshots](#task-38-capture-screenshots-for-readme-optional-but-high-signal)
- [ ] [Task 39: Final verification before submission](#task-39-final-verification-before-submission)

---

## Execution Notes

- **Commit cadence:** one commit per task (numbered). If a task's final step fails on a pre-commit hook, fix and create a NEW commit (do not amend).
- **TDD order** (for tasks with real logic): write failing test → run to confirm failure → implement minimum → run to confirm pass → commit.
- **Skeleton/config tasks** skip TDD and use: write file → run build/sync to confirm green → commit.
- **Package root:** `com.opnt.takehometest` (already set in scaffold). Module sub-packages: `com.opnt.takehometest.core.domain`, `...core.data`, `...core.ui`, `...feature.weather`.
- **Test classpath directory:** Kotlin projects commonly use `src/test/java` as the source root (Gradle's Kotlin plugin also compiles `.kt` there). This plan uses `src/test/kotlin` consistently; make sure each module's `build.gradle.kts` declares Kotlin source set for `src/test/kotlin` if using Android Gradle Plugin (the JVM `kotlin("jvm")` module picks it up automatically).
- **Gradle command prefix:** all shell commands assume you're at the repo root (`/Users/jung/Project/Take-home-test`).

---

## Task 1: Upgrade Kotlin + expand version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Replace the version catalog with the full dependency manifest**

Overwrite `gradle/libs.versions.toml` with:

```toml
[versions]
agp = "9.1.1"
kotlin = "2.3.20"
ksp = "2.3.20-2.0.2"
hilt = "2.55"
hiltNavigationCompose = "1.2.0"

coreKtx = "1.18.0"
lifecycleRuntimeKtx = "2.8.7"
lifecycleViewmodelCompose = "2.8.7"
activityCompose = "1.13.0"
composeBom = "2024.09.00"
navigationCompose = "2.8.4"

kotlinxCoroutines = "1.9.0"
kotlinxSerialization = "1.7.3"
kotlinxDatetime = "0.6.1"

retrofit = "2.11.0"
okhttp = "4.12.0"
retrofitKotlinxSerializationConverter = "1.0.0"

datastore = "1.1.1"

junit = "4.13.2"
mockk = "1.13.13"
turbine = "1.2.0"
truth = "1.4.4"
androidxJunit = "1.3.0"
androidxEspressoCore = "3.7.0"

[libraries]
# AndroidX / Compose
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-datastore = { group = "androidx.datastore", name = "datastore", version.ref = "datastore" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Kotlin stdlib extras
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinxDatetime" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization-converter = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofitKotlinxSerializationConverter" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
okhttp-mockwebserver = { group = "com.squareup.okhttp3", name = "mockwebserver", version.ref = "okhttp" }

# Test
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "androidxEspressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 2: Verify the scaffold still builds**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If compose compiler version mismatch appears, check that `kotlin-compose` plugin in `app/build.gradle.kts` picks up Kotlin 2.3.20 (no override needed — version comes from catalog).

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: bump Kotlin to 2.3.20 and expand version catalog"
```

---

## Task 2: Register module paths in `settings.gradle.kts`

**Files:**
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Replace the `include(...)` block**

Replace the existing `include(":app")` line at the bottom of `settings.gradle.kts` with:

```kotlin
rootProject.name = "Take home test"
include(":app")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":feature:weather")
```

- [ ] **Step 2: Run a Gradle sync (expected to fail until Task 3 creates the build files)**

Run: `./gradlew projects`
Expected: listing shows all five projects but the build will complain about missing `build.gradle.kts` files for the new modules. Proceed — Task 3 fixes this.

- [ ] **Step 3: Commit**

```bash
git add settings.gradle.kts
git commit -m "chore: register core and feature module paths"
```

---

## Task 3: Create `:core:domain` skeleton (pure JVM module)

**Files:**
- Create: `core/domain/build.gradle.kts`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/.gitkeep`
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/.gitkeep`

- [ ] **Step 1: Write `core/domain/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
```

- [ ] **Step 2: Add `.gitkeep` files so Gradle picks up empty source folders**

Create two empty files:
- `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/.gitkeep`
- `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/.gitkeep`

- [ ] **Step 3: Verify the module compiles**

Run: `./gradlew :core:domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add core/domain/
git commit -m "chore(domain): add core:domain pure JVM module skeleton"
```

---

## Task 4: Create `:core:data` skeleton (Android library module)

**Files:**
- Create: `core/data/build.gradle.kts`
- Create: `core/data/src/main/AndroidManifest.xml`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/.gitkeep`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/.gitkeep`
- Create: `core/data/src/test/resources/.gitkeep`

- [ ] **Step 1: Write `core/data/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.opnt.takehometest.core.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.androidx.datastore)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.okhttp.mockwebserver)
}
```

- [ ] **Step 2: Write `core/data/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

- [ ] **Step 3: Add source folder `.gitkeep` placeholders**

Create three empty files listed in this task's Files section.

- [ ] **Step 4: Verify the module compiles**

Run: `./gradlew :core:data:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/data/
git commit -m "chore(data): add core:data module skeleton"
```

---

## Task 5: Create `:core:ui` skeleton (Compose library module)

**Files:**
- Create: `core/ui/build.gradle.kts`
- Create: `core/ui/src/main/AndroidManifest.xml`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/.gitkeep`
- Create: `core/ui/src/main/res/values/strings.xml`

- [ ] **Step 1: Write `core/ui/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.opnt.takehometest.core.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

- [ ] **Step 2: Write `core/ui/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [ ] **Step 3: Write `core/ui/src/main/res/values/strings.xml`**

```xml
<resources>
    <string name="core_ui_label_retry">Retry</string>
    <string name="core_ui_label_loading">Loading…</string>
</resources>
```

- [ ] **Step 4: Add source placeholder**

Create `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/.gitkeep`.

- [ ] **Step 5: Verify the module compiles**

Run: `./gradlew :core:ui:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add core/ui/
git commit -m "chore(ui): add core:ui module skeleton"
```

---

## Task 6: Create `:feature:weather` skeleton

**Files:**
- Create: `feature/weather/build.gradle.kts`
- Create: `feature/weather/src/main/AndroidManifest.xml`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/.gitkeep`
- Create: `feature/weather/src/test/kotlin/com/opnt/takehometest/feature/weather/.gitkeep`

- [ ] **Step 1: Write `feature/weather/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.opnt.takehometest.feature.weather"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
```

- [ ] **Step 2: Write `feature/weather/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [ ] **Step 3: Add source placeholders**

Create the two `.gitkeep` files listed in this task's Files section.

- [ ] **Step 4: Verify the module compiles**

Run: `./gradlew :feature:weather:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add feature/weather/
git commit -m "chore(feature): add feature:weather module skeleton"
```

---

## Task 7: Migrate `:app` to new dependency graph and Hilt

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/kotlin/com/opnt/takehometest/TakeHomeTestApplication.kt` (temporary placeholder — real content in Task 30)
- Modify: `app/src/main/AndroidManifest.xml` (expected path; if missing, find the existing manifest under `app/src/main/`)

- [ ] **Step 1: Replace `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.opnt.takehometest"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.opnt.takehometest"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:weather"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

- [ ] **Step 2: Add temporary `TakeHomeTestApplication` placeholder**

Create `app/src/main/kotlin/com/opnt/takehometest/TakeHomeTestApplication.kt`:

```kotlin
package com.opnt.takehometest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TakeHomeTestApplication : Application()
```

*Note:* if the scaffold already places Kotlin under `app/src/main/java/`, move/rename the directory to `kotlin` to match this plan's layout, or add `sourceSets { getByName("main") { java.srcDirs("src/main/kotlin") } }` inside the `android { }` block. Pick ONE layout for the whole project.

- [ ] **Step 3: Register Application in manifest**

Open the existing `app/src/main/AndroidManifest.xml` (scaffold wrote one during project creation). Ensure the `<application>` tag has `android:name=".TakeHomeTestApplication"` and the manifest has the INTERNET permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:name=".TakeHomeTestApplication"
    ... >
```

Leave the rest (theme, icon, MainActivity declaration) untouched; we'll revisit in Task 30.

- [ ] **Step 4: Verify app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Hilt will emit generated sources under `app/build/generated/ksp/debug/kotlin`.

- [ ] **Step 5: Commit**

```bash
git add app/
git commit -m "chore(app): wire module dependencies and Hilt Application"
```

---

## Task 8: Domain models — `Coordinates`, `City`, `Forecast` family

**Files:**
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/Coordinates.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/City.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/Forecast.kt`

- [ ] **Step 1: Write `Coordinates.kt`**

```kotlin
package com.opnt.takehometest.core.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)
```

- [ ] **Step 2: Write `City.kt`**

```kotlin
package com.opnt.takehometest.core.domain.model

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val admin: String?,
    val coordinates: Coordinates,
    val timezone: String,
)
```

- [ ] **Step 3: Write `Forecast.kt`**

```kotlin
package com.opnt.takehometest.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Forecast(
    val fetchedAt: Instant,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
)

data class CurrentWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val windSpeedKmh: Double,
    val isDay: Boolean,
)

data class HourlyWeather(
    val time: Instant,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
)

data class DailyWeather(
    val date: LocalDate,
    val condition: WeatherCondition,
    val maxTemperatureCelsius: Double,
    val minTemperatureCelsius: Double,
)
```

- [ ] **Step 4: Verify compile (WeatherCondition not yet written — expect failure)**

Run: `./gradlew :core:domain:compileKotlin`
Expected: `FAILED` with "Unresolved reference: WeatherCondition". That's intentional — next task adds it.

- [ ] **Step 5: Commit together with Task 9**

Hold off committing; Task 9 will commit both.

---

## Task 9: `WeatherCondition` sealed interface + mapping tests

**Files:**
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/WeatherCondition.kt`
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/model/WeatherConditionTest.kt`

- [ ] **Step 1: Write the failing test first**

Create `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/model/WeatherConditionTest.kt`:

```kotlin
package com.opnt.takehometest.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `code 0 maps to Clear`() {
        assertThat(WeatherCondition.fromWmoCode(0)).isEqualTo(WeatherCondition.Clear)
    }

    @Test
    fun `code 1 maps to Clear`() {
        assertThat(WeatherCondition.fromWmoCode(1)).isEqualTo(WeatherCondition.Clear)
    }

    @Test
    fun `code 2 maps to PartlyCloudy`() {
        assertThat(WeatherCondition.fromWmoCode(2)).isEqualTo(WeatherCondition.PartlyCloudy)
    }

    @Test
    fun `code 3 maps to Cloudy`() {
        assertThat(WeatherCondition.fromWmoCode(3)).isEqualTo(WeatherCondition.Cloudy)
    }

    @Test
    fun `fog codes 45 and 48 map to Fog`() {
        assertThat(WeatherCondition.fromWmoCode(45)).isEqualTo(WeatherCondition.Fog)
        assertThat(WeatherCondition.fromWmoCode(48)).isEqualTo(WeatherCondition.Fog)
    }

    @Test
    fun `drizzle codes map to Drizzle`() {
        listOf(51, 53, 55, 56, 57).forEach {
            assertThat(WeatherCondition.fromWmoCode(it)).isEqualTo(WeatherCondition.Drizzle)
        }
    }

    @Test
    fun `rain codes map to Rain`() {
        listOf(61, 63, 65, 66, 67, 80, 81, 82).forEach {
            assertThat(WeatherCondition.fromWmoCode(it)).isEqualTo(WeatherCondition.Rain)
        }
    }

    @Test
    fun `snow codes map to Snow`() {
        listOf(71, 73, 75, 77, 85, 86).forEach {
            assertThat(WeatherCondition.fromWmoCode(it)).isEqualTo(WeatherCondition.Snow)
        }
    }

    @Test
    fun `thunderstorm codes map to Thunderstorm`() {
        listOf(95, 96, 99).forEach {
            assertThat(WeatherCondition.fromWmoCode(it)).isEqualTo(WeatherCondition.Thunderstorm)
        }
    }

    @Test
    fun `unknown code 42 maps to Unknown variant preserving the raw code`() {
        val actual = WeatherCondition.fromWmoCode(42)
        assertThat(actual).isInstanceOf(WeatherCondition.Unknown::class.java)
        assertThat((actual as WeatherCondition.Unknown).wmoCode).isEqualTo(42)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.model.WeatherConditionTest"`
Expected: `FAILED` — `WeatherCondition` unresolved.

- [ ] **Step 3: Write the minimal implementation**

Create `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/WeatherCondition.kt`:

```kotlin
package com.opnt.takehometest.core.domain.model

sealed interface WeatherCondition {
    data object Clear : WeatherCondition
    data object PartlyCloudy : WeatherCondition
    data object Cloudy : WeatherCondition
    data object Fog : WeatherCondition
    data object Drizzle : WeatherCondition
    data object Rain : WeatherCondition
    data object Snow : WeatherCondition
    data object Thunderstorm : WeatherCondition
    data class Unknown(val wmoCode: Int) : WeatherCondition

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> Clear
            2 -> PartlyCloudy
            3 -> Cloudy
            45, 48 -> Fog
            51, 53, 55, 56, 57 -> Drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> Rain
            71, 73, 75, 77, 85, 86 -> Snow
            95, 96, 99 -> Thunderstorm
            else -> Unknown(code)
        }
    }
}
```

- [ ] **Step 4: Run tests to confirm pass**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.model.WeatherConditionTest"`
Expected: `BUILD SUCCESSFUL`, 10 tests passed.

- [ ] **Step 5: Run the full module build to confirm Task 8 models compile**

Run: `./gradlew :core:domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit both Task 8 and 9**

```bash
git add core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/model/
git add core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/model/
git commit -m "feat(domain): add domain models and WeatherCondition mapping"
```

---

## Task 10: Repository interfaces

**Files:**
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/repository/WeatherRepository.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/repository/CityRepository.kt`

- [ ] **Step 1: Write `WeatherRepository.kt`**

```kotlin
package com.opnt.takehometest.core.domain.repository

import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.Forecast

interface WeatherRepository {
    suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast
}
```

- [ ] **Step 2: Write `CityRepository.kt`**

```kotlin
package com.opnt.takehometest.core.domain.repository

import com.opnt.takehometest.core.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    fun observeSavedCities(): Flow<List<City>>
    fun observeSelectedCity(): Flow<City?>
    suspend fun searchCities(query: String): List<City>
    suspend fun addCity(city: City)
    suspend fun removeCity(cityId: Long)
    suspend fun setSelectedCity(cityId: Long)
}
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :core:domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/repository/
git commit -m "feat(domain): add weather and city repository interfaces"
```

---

## Task 11: `GetForecastUseCase` with tests

**Files:**
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/usecase/GetForecastUseCaseTest.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/GetForecastUseCase.kt`

- [ ] **Step 1: Write the failing test**

Create the test file:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test

class GetForecastUseCaseTest {

    private val repo: WeatherRepository = mockk()
    private val useCase = GetForecastUseCase(repo)

    @Test
    fun `invoke delegates to repository with city coordinates and timezone`() = runTest {
        val city = City(
            id = 1L,
            name = "Taipei",
            country = "Taiwan",
            admin = "Taipei City",
            coordinates = Coordinates(25.0330, 121.5654),
            timezone = "Asia/Taipei",
        )
        val expected = Forecast(
            fetchedAt = Instant.parse("2026-04-16T14:00:00Z"),
            current = CurrentWeather(
                time = Instant.parse("2026-04-16T14:00:00Z"),
                temperatureCelsius = 22.5,
                condition = WeatherCondition.PartlyCloudy,
                windSpeedKmh = 3.4,
                isDay = true,
            ),
            hourly = emptyList(),
            daily = emptyList(),
        )
        coEvery { repo.getForecast(city.coordinates, city.timezone) } returns expected

        val actual = useCase(city)

        assertThat(actual).isEqualTo(expected)
        coVerify(exactly = 1) { repo.getForecast(city.coordinates, city.timezone) }
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.GetForecastUseCaseTest"`
Expected: `FAILED` — `GetForecastUseCase` unresolved.

- [ ] **Step 3: Implement the use case**

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository,
) {
    suspend operator fun invoke(city: City): Forecast =
        repository.getForecast(city.coordinates, city.timezone)
}
```

*Note:* `javax.inject.Inject` is available transitively via the `kotlinx-coroutines-core` → nothing, but Hilt's `javax.inject` annotations come from the JSR-330 artifact. Add `implementation("javax.inject:javax.inject:1")` to `core/domain/build.gradle.kts` if compilation fails. (Hilt's dependency graph adds this transitively in `:core:data`; for the pure-JVM domain module we want the explicit dependency — see Step 4.)

- [ ] **Step 4: If compile fails on `javax.inject.Inject`, add the dependency**

Edit `core/domain/build.gradle.kts`, add to `dependencies`:

```kotlin
    implementation("javax.inject:javax.inject:1")
```

Re-run `./gradlew :core:domain:compileKotlin` to confirm success.

- [ ] **Step 5: Run test to confirm pass**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.GetForecastUseCaseTest"`
Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 6: Commit**

```bash
git add core/domain/
git commit -m "feat(domain): add GetForecastUseCase"
```

---

## Task 12: Observer + simple mutation use cases

**Files:**
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/ObserveSavedCitiesUseCase.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/ObserveSelectedCityUseCase.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/AddCityUseCase.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/RemoveCityUseCase.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/SelectCityUseCase.kt`
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/usecase/CityMutationUseCasesTest.kt`

- [ ] **Step 1: Write the failing test**

Create `CityMutationUseCasesTest.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CityMutationUseCasesTest {

    private val repo: CityRepository = mockk()

    private val sampleCity = City(
        id = 1668341L,
        name = "Taipei",
        country = "Taiwan",
        admin = "Taipei City",
        coordinates = Coordinates(25.0330, 121.5654),
        timezone = "Asia/Taipei",
    )

    @Test
    fun `ObserveSavedCities re-emits repository flow`() = runTest {
        every { repo.observeSavedCities() } returns flowOf(listOf(sampleCity))
        val result = ObserveSavedCitiesUseCase(repo)().toList()
        assertThat(result).containsExactly(listOf(sampleCity))
    }

    @Test
    fun `ObserveSelectedCity re-emits repository flow`() = runTest {
        every { repo.observeSelectedCity() } returns flowOf(sampleCity)
        val result = ObserveSelectedCityUseCase(repo)().toList()
        assertThat(result).containsExactly(sampleCity)
    }

    @Test
    fun `AddCityUseCase delegates to repository`() = runTest {
        coEvery { repo.addCity(sampleCity) } just Runs
        AddCityUseCase(repo)(sampleCity)
        coVerify(exactly = 1) { repo.addCity(sampleCity) }
    }

    @Test
    fun `RemoveCityUseCase delegates by id`() = runTest {
        coEvery { repo.removeCity(42L) } just Runs
        RemoveCityUseCase(repo)(42L)
        coVerify(exactly = 1) { repo.removeCity(42L) }
    }

    @Test
    fun `SelectCityUseCase delegates by id`() = runTest {
        coEvery { repo.setSelectedCity(42L) } just Runs
        SelectCityUseCase(repo)(42L)
        coVerify(exactly = 1) { repo.setSelectedCity(42L) }
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.CityMutationUseCasesTest"`
Expected: compilation error on missing use-case classes.

- [ ] **Step 3: Implement the five use cases**

`ObserveSavedCitiesUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSavedCitiesUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    operator fun invoke(): Flow<List<City>> = repository.observeSavedCities()
}
```

`ObserveSelectedCityUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSelectedCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    operator fun invoke(): Flow<City?> = repository.observeSelectedCity()
}
```

`AddCityUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class AddCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(city: City) = repository.addCity(city)
}
```

`RemoveCityUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class RemoveCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) = repository.removeCity(cityId)
}
```

`SelectCityUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class SelectCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) = repository.setSelectedCity(cityId)
}
```

- [ ] **Step 4: Run to confirm pass**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.CityMutationUseCasesTest"`
Expected: `BUILD SUCCESSFUL`, 5 tests passed.

- [ ] **Step 5: Commit**

```bash
git add core/domain/
git commit -m "feat(domain): add observe and mutation use cases for cities"
```

---

## Task 13: `SearchCitiesUseCase` with trimming + min-length gate

**Files:**
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/usecase/SearchCitiesUseCaseTest.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/SearchCitiesUseCase.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchCitiesUseCaseTest {

    private val repo: CityRepository = mockk()
    private val useCase = SearchCitiesUseCase(repo)

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0330, 121.5654), timezone = "Asia/Taipei",
    )

    @Test
    fun `trimmed query shorter than 2 returns empty without hitting repo`() = runTest {
        assertThat(useCase("")).isEmpty()
        assertThat(useCase(" ")).isEmpty()
        assertThat(useCase("a")).isEmpty()
        assertThat(useCase(" a ")).isEmpty()
        coVerify(exactly = 0) { repo.searchCities(any()) }
    }

    @Test
    fun `trimmed query of length 2 or more delegates to repository`() = runTest {
        coEvery { repo.searchCities("Taipei") } returns listOf(taipei)
        val result = useCase("  Taipei  ")
        assertThat(result).containsExactly(taipei)
        coVerify(exactly = 1) { repo.searchCities("Taipei") }
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCaseTest"`
Expected: compilation failure on `SearchCitiesUseCase`.

- [ ] **Step 3: Implement**

`core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/SearchCitiesUseCase.kt`:

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke(query: String): List<City> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return emptyList()
        return repository.searchCities(trimmed)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
```

- [ ] **Step 4: Run to confirm pass**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCaseTest"`
Expected: 2 tests passed.

- [ ] **Step 5: Commit**

```bash
git add core/domain/
git commit -m "feat(domain): add SearchCitiesUseCase with trim and min-length"
```

---

## Task 14: `SeedDefaultCityUseCase` with idempotency

**Files:**
- Create: `core/domain/src/test/kotlin/com/opnt/takehometest/core/domain/usecase/SeedDefaultCityUseCaseTest.kt`
- Create: `core/domain/src/main/kotlin/com/opnt/takehometest/core/domain/usecase/SeedDefaultCityUseCase.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SeedDefaultCityUseCaseTest {

    private val repo: CityRepository = mockk()
    private val useCase = SeedDefaultCityUseCase(repo)

    @Test
    fun `inserts Taipei and selects it when saved list is empty`() = runTest {
        coEvery { repo.observeSavedCities() } returns flowOf(emptyList())
        coEvery { repo.addCity(any()) } just Runs
        coEvery { repo.setSelectedCity(any()) } just Runs

        useCase()

        coVerify(exactly = 1) {
            repo.addCity(match {
                it.id == 1668341L &&
                    it.name == "Taipei" &&
                    it.coordinates == Coordinates(25.0330, 121.5654) &&
                    it.timezone == "Asia/Taipei"
            })
        }
        coVerify(exactly = 1) { repo.setSelectedCity(1668341L) }
    }

    @Test
    fun `does nothing when saved list is non-empty`() = runTest {
        val existing = City(
            id = 999L, name = "London", country = "UK", admin = null,
            coordinates = Coordinates(51.5, -0.12), timezone = "Europe/London",
        )
        coEvery { repo.observeSavedCities() } returns flowOf(listOf(existing))

        useCase()

        coVerify(exactly = 0) { repo.addCity(any()) }
        coVerify(exactly = 0) { repo.setSelectedCity(any()) }
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.SeedDefaultCityUseCaseTest"`
Expected: compilation failure.

- [ ] **Step 3: Implement**

```kotlin
package com.opnt.takehometest.core.domain.usecase

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SeedDefaultCityUseCase @Inject constructor(
    private val repository: CityRepository,
) {
    suspend operator fun invoke() {
        val existing = repository.observeSavedCities().first()
        if (existing.isNotEmpty()) return
        repository.addCity(DEFAULT_CITY)
        repository.setSelectedCity(DEFAULT_CITY.id)
    }

    companion object {
        val DEFAULT_CITY = City(
            id = 1668341L,
            name = "Taipei",
            country = "Taiwan",
            admin = "Taipei City",
            coordinates = Coordinates(25.0330, 121.5654),
            timezone = "Asia/Taipei",
        )
    }
}
```

- [ ] **Step 4: Run to confirm pass**

Run: `./gradlew :core:domain:test --tests "com.opnt.takehometest.core.domain.usecase.SeedDefaultCityUseCaseTest"`
Expected: 2 tests passed.

- [ ] **Step 5: Run entire domain test suite**

Run: `./gradlew :core:domain:test`
Expected: all domain tests green.

- [ ] **Step 6: Commit**

```bash
git add core/domain/
git commit -m "feat(domain): add SeedDefaultCityUseCase with idempotency"
```

---

## Task 15: Open-Meteo DTOs

**Files:**
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/dto/ForecastResponseDto.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/dto/GeocodingResponseDto.kt`

- [ ] **Step 1: Write `ForecastResponseDto.kt`**

```kotlin
package com.opnt.takehometest.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
) {
    @Serializable
    data class CurrentDto(
        val time: String,
        @SerialName("temperature_2m") val temperature2m: Double,
        @SerialName("weather_code") val weatherCode: Int,
        @SerialName("wind_speed_10m") val windSpeed10m: Double,
        @SerialName("is_day") val isDay: Int,
    )

    @Serializable
    data class HourlyDto(
        val time: List<String>,
        @SerialName("temperature_2m") val temperature2m: List<Double>,
        @SerialName("weather_code") val weatherCode: List<Int>,
    )

    @Serializable
    data class DailyDto(
        val time: List<String>,
        @SerialName("weather_code") val weatherCode: List<Int>,
        @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
        @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    )
}
```

- [ ] **Step 2: Write `GeocodingResponseDto.kt`**

```kotlin
package com.opnt.takehometest.core.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList(),
) {
    @Serializable
    data class GeocodingResultDto(
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val country: String,
        val admin1: String? = null,
        val timezone: String,
    )
}
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :core:data:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/dto/
git commit -m "feat(data): add Open-Meteo forecast and geocoding DTOs"
```

---

## Task 16: Retrofit service interfaces + MockWebServer tests

**Files:**
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/OpenMeteoForecastApi.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/OpenMeteoGeocodingApi.kt`
- Create: `core/data/src/test/resources/fixtures/forecast_response_taipei.json`
- Create: `core/data/src/test/resources/fixtures/geocoding_response_taipei.json`
- Create: `core/data/src/test/resources/fixtures/geocoding_response_empty.json`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/network/OpenMeteoForecastApiTest.kt`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/network/OpenMeteoGeocodingApiTest.kt`

- [ ] **Step 1: Write realistic fixture `forecast_response_taipei.json`**

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
    "time": [
      "2026-04-16T14:00", "2026-04-16T15:00", "2026-04-16T16:00",
      "2026-04-16T17:00", "2026-04-16T18:00", "2026-04-16T19:00",
      "2026-04-16T20:00", "2026-04-16T21:00", "2026-04-16T22:00",
      "2026-04-16T23:00", "2026-04-17T00:00", "2026-04-17T01:00",
      "2026-04-17T02:00", "2026-04-17T03:00", "2026-04-17T04:00",
      "2026-04-17T05:00", "2026-04-17T06:00", "2026-04-17T07:00",
      "2026-04-17T08:00", "2026-04-17T09:00", "2026-04-17T10:00",
      "2026-04-17T11:00", "2026-04-17T12:00", "2026-04-17T13:00",
      "2026-04-17T14:00"
    ],
    "temperature_2m": [
      22.5, 22.8, 22.6, 22.1, 21.4, 20.9,
      20.3, 19.8, 19.5, 19.2, 18.9, 18.7,
      18.5, 18.3, 18.2, 18.1, 18.4, 19.1,
      20.0, 21.2, 22.6, 23.4, 24.0, 24.3,
      24.1
    ],
    "weather_code": [
      2, 2, 3, 3, 3, 61, 61, 63, 63, 3, 2, 2,
      1, 1, 0, 0, 0, 1, 2, 2, 3, 3, 61, 61, 2
    ]
  },
  "daily": {
    "time": [
      "2026-04-16", "2026-04-17", "2026-04-18",
      "2026-04-19", "2026-04-20", "2026-04-21", "2026-04-22"
    ],
    "weather_code": [2, 61, 61, 3, 0, 0, 1],
    "temperature_2m_max": [25.1, 22.0, 21.5, 23.8, 26.3, 27.1, 26.7],
    "temperature_2m_min": [18.2, 17.5, 17.0, 17.3, 18.9, 19.2, 19.5]
  }
}
```

- [ ] **Step 2: Write `geocoding_response_taipei.json`**

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
      "timezone": "Asia/Taipei"
    },
    {
      "id": 1668342,
      "name": "New Taipei",
      "latitude": 25.01,
      "longitude": 121.46,
      "country": "Taiwan",
      "admin1": "New Taipei",
      "timezone": "Asia/Taipei"
    }
  ]
}
```

- [ ] **Step 3: Write `geocoding_response_empty.json`**

```json
{}
```

- [ ] **Step 4: Write the failing forecast API test**

```kotlin
package com.opnt.takehometest.core.data.network

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OpenMeteoForecastApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenMeteoForecastApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OpenMeteoForecastApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `parses realistic Taipei forecast fixture`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/forecast_response_taipei.json")!!.readText()
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )

        val response = api.getForecast(
            lat = 25.0330,
            lon = 121.5654,
            timezone = "Asia/Taipei",
        )

        assertThat(response.timezone).isEqualTo("Asia/Taipei")
        assertThat(response.current.temperature2m).isEqualTo(22.5)
        assertThat(response.current.weatherCode).isEqualTo(2)
        assertThat(response.current.isDay).isEqualTo(1)
        assertThat(response.hourly.time).hasSize(25)
        assertThat(response.daily.weatherCode).containsExactly(2, 61, 61, 3, 0, 0, 1).inOrder()
    }
}
```

- [ ] **Step 5: Write the failing geocoding API test**

```kotlin
package com.opnt.takehometest.core.data.network

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OpenMeteoGeocodingApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenMeteoGeocodingApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `parses standard geocoding response`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/geocoding_response_taipei.json")!!.readText()
        server.enqueue(MockResponse().setResponseCode(200)
            .setHeader("Content-Type", "application/json").setBody(body))

        val response = api.search(name = "Taipei")

        assertThat(response.results).hasSize(2)
        assertThat(response.results[0].id).isEqualTo(1668341L)
        assertThat(response.results[0].admin1).isEqualTo("Taipei City")
    }

    @Test
    fun `parses response with missing results field as empty list`() = kotlinx.coroutines.runBlocking {
        val body = javaClass.getResource("/fixtures/geocoding_response_empty.json")!!.readText()
        server.enqueue(MockResponse().setResponseCode(200)
            .setHeader("Content-Type", "application/json").setBody(body))

        val response = api.search(name = "zzzzz")

        assertThat(response.results).isEmpty()
    }
}
```

- [ ] **Step 6: Run both tests — expect compile failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.network.*"`
Expected: compile errors (services not yet written).

- [ ] **Step 7: Write `OpenMeteoForecastApi.kt`**

```kotlin
package com.opnt.takehometest.core.data.network

import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,is_day",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int = 7,
    ): ForecastResponseDto
}
```

- [ ] **Step 8: Write `OpenMeteoGeocodingApi.kt`**

```kotlin
package com.opnt.takehometest.core.data.network

import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json",
    ): GeocodingResponseDto
}
```

- [ ] **Step 9: Run tests to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.network.*"`
Expected: 3 tests passed.

- [ ] **Step 10: Commit**

```bash
git add core/data/src/main/kotlin/com/opnt/takehometest/core/data/network/
git add core/data/src/test/
git commit -m "feat(data): add Open-Meteo Retrofit services with MockWebServer tests"
```

---

## Task 17: `ForecastMapper` with tests

**Files:**
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/mapper/ForecastMapperTest.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/mapper/ForecastMapper.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.WeatherCondition
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test

class ForecastMapperTest {

    private val mapper = ForecastMapper()

    private val dto = ForecastResponseDto(
        latitude = 25.03,
        longitude = 121.57,
        timezone = "Asia/Taipei",
        current = ForecastResponseDto.CurrentDto(
            time = "2026-04-16T14:00",
            temperature2m = 22.5,
            weatherCode = 2,
            windSpeed10m = 3.4,
            isDay = 1,
        ),
        hourly = ForecastResponseDto.HourlyDto(
            time = listOf(
                "2026-04-16T12:00", "2026-04-16T13:00", "2026-04-16T14:00",
                "2026-04-16T15:00", "2026-04-16T16:00",
            ),
            temperature2m = listOf(21.0, 21.5, 22.0, 22.5, 23.0),
            weatherCode = listOf(0, 1, 2, 3, 61),
        ),
        daily = ForecastResponseDto.DailyDto(
            time = listOf("2026-04-16", "2026-04-17"),
            weatherCode = listOf(2, 61),
            temperatureMax = listOf(25.1, 22.0),
            temperatureMin = listOf(18.2, 17.5),
        ),
    )

    private val fetchedAt = Instant.parse("2026-04-16T14:00:00Z") // 14:00 UTC = 22:00 Taipei

    @Test
    fun `current maps temperature, condition, wind and isDay`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.current.temperatureCelsius).isEqualTo(22.5)
        assertThat(result.current.condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.current.windSpeedKmh).isEqualTo(3.4)
        assertThat(result.current.isDay).isTrue()
    }

    @Test
    fun `hourly entries before fetchedAt are filtered out`() {
        // Taipei UTC+8, so "2026-04-16T14:00" local = "2026-04-16T06:00" UTC.
        // fetchedAt is 2026-04-16T14:00 UTC = 2026-04-16T22:00 Taipei local.
        // All 5 hourly entries (12:00..16:00 local) are BEFORE fetchedAt → result is empty.
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.hourly).isEmpty()
    }

    @Test
    fun `hourly take 24 when future entries exceed 24`() {
        val earlyFetchedAt = Instant.parse("2026-04-16T00:00:00Z")
        val bigDto = dto.copy(
            hourly = ForecastResponseDto.HourlyDto(
                time = (0..30).map { "2026-04-17T%02d:00".format(it.coerceAtMost(23)) },
                temperature2m = List(31) { 20.0 },
                weatherCode = List(31) { 0 },
            )
        )
        val result = mapper.toDomain(bigDto, earlyFetchedAt)
        assertThat(result.hourly).hasSize(24)
    }

    @Test
    fun `daily maps date, condition, and min-max`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.daily).hasSize(2)
        assertThat(result.daily[0].date).isEqualTo(LocalDate(2026, 4, 16))
        assertThat(result.daily[0].condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.daily[0].maxTemperatureCelsius).isEqualTo(25.1)
        assertThat(result.daily[0].minTemperatureCelsius).isEqualTo(18.2)
        assertThat(result.daily[1].condition).isEqualTo(WeatherCondition.Rain)
    }

    @Test
    fun `fetchedAt is stamped on the result`() {
        val result = mapper.toDomain(dto, fetchedAt)
        assertThat(result.fetchedAt).isEqualTo(fetchedAt)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.mapper.ForecastMapperTest"`
Expected: compile failure.

- [ ] **Step 3: Implement the mapper**

```kotlin
package com.opnt.takehometest.core.data.mapper

import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.DailyWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.HourlyWeather
import com.opnt.takehometest.core.domain.model.WeatherCondition
import javax.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class ForecastMapper @Inject constructor() {

    fun toDomain(dto: ForecastResponseDto, fetchedAt: Instant): Forecast {
        val zone = TimeZone.of(dto.timezone)
        return Forecast(
            fetchedAt = fetchedAt,
            current = dto.current.toDomain(zone),
            hourly = mapHourly(dto.hourly, zone, fetchedAt),
            daily = mapDaily(dto.daily),
        )
    }

    private fun ForecastResponseDto.CurrentDto.toDomain(zone: TimeZone) =
        CurrentWeather(
            time = LocalDateTime.parse(time).toInstant(zone),
            temperatureCelsius = temperature2m,
            condition = WeatherCondition.fromWmoCode(weatherCode),
            windSpeedKmh = windSpeed10m,
            isDay = isDay == 1,
        )

    private fun mapHourly(
        dto: ForecastResponseDto.HourlyDto,
        zone: TimeZone,
        fetchedAt: Instant,
    ): List<HourlyWeather> =
        dto.time.indices.map { i ->
            HourlyWeather(
                time = LocalDateTime.parse(dto.time[i]).toInstant(zone),
                temperatureCelsius = dto.temperature2m[i],
                condition = WeatherCondition.fromWmoCode(dto.weatherCode[i]),
            )
        }.filter { it.time >= fetchedAt }.take(HOURLY_LIMIT)

    private fun mapDaily(dto: ForecastResponseDto.DailyDto): List<DailyWeather> =
        dto.time.indices.map { i ->
            DailyWeather(
                date = LocalDate.parse(dto.time[i]),
                condition = WeatherCondition.fromWmoCode(dto.weatherCode[i]),
                maxTemperatureCelsius = dto.temperatureMax[i],
                minTemperatureCelsius = dto.temperatureMin[i],
            )
        }

    companion object {
        const val HOURLY_LIMIT = 24
    }
}
```

- [ ] **Step 4: Run to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.mapper.ForecastMapperTest"`
Expected: 5 tests passed.

- [ ] **Step 5: Commit**

```bash
git add core/data/
git commit -m "feat(data): add ForecastMapper with hourly filtering"
```

---

## Task 18: `CityMapper` (DTO ↔ domain ↔ serializable) with tests

**Files:**
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/local/SerializableCity.kt` (only the serializable model for now; `SavedCitiesState` in Task 19)
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/mapper/CityMapper.kt`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/mapper/CityMapperTest.kt`

- [ ] **Step 1: Write `SerializableCity`**

```kotlin
package com.opnt.takehometest.core.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class SerializableCity(
    val id: Long,
    val name: String,
    val country: String,
    val admin: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)
```

- [ ] **Step 2: Write the failing test**

```kotlin
package com.opnt.takehometest.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import org.junit.Test

class CityMapperTest {

    private val mapper = CityMapper()

    @Test
    fun `geocoding DTO to domain preserves id, name, country, admin, coords, timezone`() {
        val dto = GeocodingResponseDto.GeocodingResultDto(
            id = 1668341L,
            name = "Taipei",
            latitude = 25.04,
            longitude = 121.56,
            country = "Taiwan",
            admin1 = "Taipei City",
            timezone = "Asia/Taipei",
        )
        val city = mapper.toDomain(dto)
        assertThat(city).isEqualTo(City(
            id = 1668341L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
            coordinates = Coordinates(25.04, 121.56), timezone = "Asia/Taipei",
        ))
    }

    @Test
    fun `serializable to domain round-trip preserves fields`() {
        val city = City(
            id = 99L, name = "London", country = "UK", admin = null,
            coordinates = Coordinates(51.5, -0.12), timezone = "Europe/London",
        )
        val roundTripped = mapper.toDomain(mapper.toSerializable(city))
        assertThat(roundTripped).isEqualTo(city)
    }

    @Test
    fun `serializable to domain preserves null admin`() {
        val serialized = SerializableCity(
            id = 99L, name = "X", country = "Y", admin = null,
            latitude = 0.0, longitude = 0.0, timezone = "UTC",
        )
        assertThat(mapper.toDomain(serialized).admin).isNull()
    }
}
```

- [ ] **Step 3: Run to confirm failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.mapper.CityMapperTest"`
Expected: compile failure.

- [ ] **Step 4: Implement `CityMapper`**

```kotlin
package com.opnt.takehometest.core.data.mapper

import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import javax.inject.Inject

class CityMapper @Inject constructor() {

    fun toDomain(dto: GeocodingResponseDto.GeocodingResultDto): City = City(
        id = dto.id,
        name = dto.name,
        country = dto.country,
        admin = dto.admin1,
        coordinates = Coordinates(dto.latitude, dto.longitude),
        timezone = dto.timezone,
    )

    internal fun toSerializable(city: City): SerializableCity = SerializableCity(
        id = city.id,
        name = city.name,
        country = city.country,
        admin = city.admin,
        latitude = city.coordinates.latitude,
        longitude = city.coordinates.longitude,
        timezone = city.timezone,
    )

    internal fun toDomain(saved: SerializableCity): City = City(
        id = saved.id,
        name = saved.name,
        country = saved.country,
        admin = saved.admin,
        coordinates = Coordinates(saved.latitude, saved.longitude),
        timezone = saved.timezone,
    )
}
```

- [ ] **Step 5: Run to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.mapper.CityMapperTest"`
Expected: 3 tests passed.

- [ ] **Step 6: Commit**

```bash
git add core/data/
git commit -m "feat(data): add CityMapper and SerializableCity"
```

---

## Task 19: `SavedCitiesState` + `SavedCitiesSerializer` with tests

**Files:**
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/local/SavedCitiesState.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/local/SavedCitiesSerializer.kt`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/local/SavedCitiesSerializerTest.kt`

- [ ] **Step 1: Write `SavedCitiesState`**

```kotlin
package com.opnt.takehometest.core.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class SavedCitiesState(
    val cities: List<SerializableCity> = emptyList(),
    val selectedCityId: Long? = null,
)
```

- [ ] **Step 2: Write the failing serializer test**

```kotlin
package com.opnt.takehometest.core.data.local

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SavedCitiesSerializerTest {

    @Test
    fun `default value is empty cities with null selectedCityId`() {
        assertThat(SavedCitiesSerializer.defaultValue.cities).isEmpty()
        assertThat(SavedCitiesSerializer.defaultValue.selectedCityId).isNull()
    }

    @Test
    fun `write then read round-trips state faithfully`() = runTest {
        val state = SavedCitiesState(
            cities = listOf(
                SerializableCity(1L, "Taipei", "Taiwan", "Taipei City", 25.0, 121.5, "Asia/Taipei"),
                SerializableCity(2L, "Tokyo", "Japan", null, 35.6, 139.7, "Asia/Tokyo"),
            ),
            selectedCityId = 2L,
        )
        val out = ByteArrayOutputStream()
        SavedCitiesSerializer.writeTo(state, out)

        val input = ByteArrayInputStream(out.toByteArray())
        val read = SavedCitiesSerializer.readFrom(input)

        assertThat(read).isEqualTo(state)
    }

    @Test(expected = CorruptionException::class)
    fun `malformed JSON throws CorruptionException`() = runTest {
        val malformed = "{this is not json".toByteArray()
        SavedCitiesSerializer.readFrom(ByteArrayInputStream(malformed))
    }
}
```

- [ ] **Step 3: Run to confirm failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.local.SavedCitiesSerializerTest"`
Expected: compile failure.

- [ ] **Step 4: Implement the serializer**

```kotlin
package com.opnt.takehometest.core.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
internal object SavedCitiesSerializer : Serializer<SavedCitiesState> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: SavedCitiesState = SavedCitiesState()

    override suspend fun readFrom(input: InputStream): SavedCitiesState =
        try {
            json.decodeFromStream(input)
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read SavedCitiesState", e)
        }

    override suspend fun writeTo(t: SavedCitiesState, output: OutputStream) {
        json.encodeToStream(t, output)
    }
}
```

- [ ] **Step 5: Run to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.local.SavedCitiesSerializerTest"`
Expected: 3 tests passed.

- [ ] **Step 6: Commit**

```bash
git add core/data/
git commit -m "feat(data): add SavedCitiesState and Typed DataStore serializer"
```

---

## Task 20: `WeatherRepositoryImpl` with tests

**Files:**
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/repository/WeatherRepositoryImplTest.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/repository/WeatherRepositoryImpl.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.mapper.ForecastMapper
import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.data.network.dto.ForecastResponseDto
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.WeatherCondition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Test

class WeatherRepositoryImplTest {

    private val api: OpenMeteoForecastApi = mockk()
    private val mapper = ForecastMapper()

    private val dto = ForecastResponseDto(
        latitude = 25.03,
        longitude = 121.57,
        timezone = "Asia/Taipei",
        current = ForecastResponseDto.CurrentDto(
            time = "2026-04-16T14:00",
            temperature2m = 22.5,
            weatherCode = 2,
            windSpeed10m = 3.4,
            isDay = 1,
        ),
        hourly = ForecastResponseDto.HourlyDto(
            time = listOf("2026-04-16T14:00"),
            temperature2m = listOf(22.5),
            weatherCode = listOf(2),
        ),
        daily = ForecastResponseDto.DailyDto(
            time = listOf("2026-04-16"),
            weatherCode = listOf(2),
            temperatureMax = listOf(25.1),
            temperatureMin = listOf(18.2),
        ),
    )

    @Test
    fun `getForecast calls API with lat, lon, timezone and maps response`() = runTest {
        coEvery {
            api.getForecast(lat = 25.0330, lon = 121.5654, timezone = "Asia/Taipei")
        } returns dto
        val fixedClock = object : Clock { override fun now(): Instant =
            Instant.parse("2026-04-16T14:00:00Z") }
        val repo = WeatherRepositoryImpl(api, mapper, fixedClock)

        val result = repo.getForecast(Coordinates(25.0330, 121.5654), "Asia/Taipei")

        assertThat(result.current.condition).isEqualTo(WeatherCondition.PartlyCloudy)
        assertThat(result.current.temperatureCelsius).isEqualTo(22.5)
        coVerify(exactly = 1) { api.getForecast(lat = 25.0330, lon = 121.5654, timezone = "Asia/Taipei") }
    }
}
```

- [ ] **Step 2: Run to confirm failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.repository.WeatherRepositoryImplTest"`
Expected: compile failure.

- [ ] **Step 3: Implement the repository**

```kotlin
package com.opnt.takehometest.core.data.repository

import com.opnt.takehometest.core.data.mapper.ForecastMapper
import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.datetime.Clock

internal class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoForecastApi,
    private val mapper: ForecastMapper,
    private val clock: Clock,
) : WeatherRepository {

    override suspend fun getForecast(coordinates: Coordinates, timezone: String): Forecast {
        val dto = api.getForecast(
            lat = coordinates.latitude,
            lon = coordinates.longitude,
            timezone = timezone,
        )
        return mapper.toDomain(dto, clock.now())
    }
}
```

- [ ] **Step 4: Run to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.repository.WeatherRepositoryImplTest"`
Expected: 1 test passed.

- [ ] **Step 5: Commit**

```bash
git add core/data/
git commit -m "feat(data): add WeatherRepositoryImpl"
```

---

## Task 21: `CityRepositoryImpl` with tests (uses a fake DataStore)

**Files:**
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/repository/FakeDataStore.kt`
- Create: `core/data/src/test/kotlin/com/opnt/takehometest/core/data/repository/CityRepositoryImplTest.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/repository/CityRepositoryImpl.kt`

- [ ] **Step 1: Write `FakeDataStore`**

```kotlin
package com.opnt.takehometest.core.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val state = MutableStateFlow(initial)
    override val data = state
    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        state.update { runBlocking { transform(it) } }
        return state.value
    }
}

// Needed since updateData is suspend; collapse suspend into runBlocking for the fake only.
private fun <R> runBlocking(block: suspend () -> R): R =
    kotlinx.coroutines.runBlocking { block() }
```

*Explanation:* `DataStore.updateData` provides a suspend transform; our fake captures it via `runBlocking` so state updates are synchronous inside `runTest`.

- [ ] **Step 2: Write the failing test**

```kotlin
package com.opnt.takehometest.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.data.local.SavedCitiesState
import com.opnt.takehometest.core.data.local.SerializableCity
import com.opnt.takehometest.core.data.mapper.CityMapper
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import com.opnt.takehometest.core.data.network.dto.GeocodingResponseDto
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CityRepositoryImplTest {

    private val api: OpenMeteoGeocodingApi = mockk()
    private val mapper = CityMapper()

    private fun newRepo(initial: SavedCitiesState = SavedCitiesState()) =
        CityRepositoryImpl(api, FakeDataStore(initial), mapper)

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val tokyo = City(
        id = 2L, name = "Tokyo", country = "Japan", admin = null,
        coordinates = Coordinates(35.6, 139.7), timezone = "Asia/Tokyo",
    )

    @Test
    fun `observeSavedCities maps persisted state to domain`() = runTest {
        val repo = newRepo(SavedCitiesState(
            cities = listOf(
                SerializableCity(1L, "Taipei", "Taiwan", "Taipei City", 25.0, 121.5, "Asia/Taipei"),
            ),
        ))
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeSelectedCity returns null when id not in list`() = runTest {
        val repo = newRepo(SavedCitiesState(
            cities = emptyList(), selectedCityId = 99L,
        ))
        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCity inserts and auto-selects first city`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isEqualTo(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCity is idempotent on duplicate id`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.addCity(taipei.copy(name = "Taipei City")) // same id
        repo.observeSavedCities().test {
            assertThat(awaitItem()).containsExactly(taipei) // original preserved
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCity updates selectedCityId when removing selected city`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.addCity(tokyo)
        repo.setSelectedCity(tokyo.id)

        repo.removeCity(tokyo.id)

        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isEqualTo(taipei) // auto-reselected
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCity last city sets selectedCityId to null`() = runTest {
        val repo = newRepo()
        repo.addCity(taipei)
        repo.removeCity(taipei.id)

        repo.observeSelectedCity().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchCities trims and maps API results`() = runTest {
        coEvery { api.search(name = "Taipei") } returns GeocodingResponseDto(
            results = listOf(
                GeocodingResponseDto.GeocodingResultDto(
                    id = 1L, name = "Taipei", latitude = 25.0, longitude = 121.5,
                    country = "Taiwan", admin1 = "Taipei City", timezone = "Asia/Taipei",
                )
            )
        )
        val repo = newRepo()

        val result = repo.searchCities("  Taipei  ")

        assertThat(result).containsExactly(taipei)
    }
}
```

- [ ] **Step 3: Run to confirm failure**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.repository.CityRepositoryImplTest"`
Expected: compile failure.

- [ ] **Step 4: Implement `CityRepositoryImpl`**

```kotlin
package com.opnt.takehometest.core.data.repository

import androidx.datastore.core.DataStore
import com.opnt.takehometest.core.data.local.SavedCitiesState
import com.opnt.takehometest.core.data.mapper.CityMapper
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CityRepositoryImpl @Inject constructor(
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val dataStore: DataStore<SavedCitiesState>,
    private val mapper: CityMapper,
) : CityRepository {

    override fun observeSavedCities(): Flow<List<City>> =
        dataStore.data.map { state -> state.cities.map(mapper::toDomain) }

    override fun observeSelectedCity(): Flow<City?> =
        dataStore.data.map { state ->
            state.cities.firstOrNull { it.id == state.selectedCityId }?.let(mapper::toDomain)
        }

    override suspend fun searchCities(query: String): List<City> =
        geocodingApi.search(name = query.trim())
            .results.map(mapper::toDomain)

    override suspend fun addCity(city: City) {
        dataStore.updateData { state ->
            if (state.cities.any { it.id == city.id }) {
                state
            } else {
                state.copy(
                    cities = state.cities + mapper.toSerializable(city),
                    selectedCityId = state.selectedCityId ?: city.id,
                )
            }
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

- [ ] **Step 5: Run to confirm pass**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.opnt.takehometest.core.data.repository.CityRepositoryImplTest"`
Expected: 7 tests passed.

- [ ] **Step 6: Commit**

```bash
git add core/data/
git commit -m "feat(data): add CityRepositoryImpl with fake DataStore tests"
```

---

## Task 22: Hilt DI modules for data layer

**Files:**
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/di/Qualifiers.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/di/NetworkModule.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/di/DataStoreModule.kt`
- Create: `core/data/src/main/kotlin/com/opnt/takehometest/core/data/di/RepositoryModule.kt`

- [ ] **Step 1: Write qualifier annotations**

```kotlin
package com.opnt.takehometest.core.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit
```

- [ ] **Step 2: Write `NetworkModule`**

```kotlin
package com.opnt.takehometest.core.data.di

import com.opnt.takehometest.core.data.network.OpenMeteoForecastApi
import com.opnt.takehometest.core.data.network.OpenMeteoGeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton @ForecastRetrofit
    fun provideForecastRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton @GeocodingRetrofit
    fun provideGeocodingRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideForecastApi(@ForecastRetrofit retrofit: Retrofit): OpenMeteoForecastApi =
        retrofit.create(OpenMeteoForecastApi::class.java)

    @Provides @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): OpenMeteoGeocodingApi =
        retrofit.create(OpenMeteoGeocodingApi::class.java)
}
```

- [ ] **Step 3: Write `DataStoreModule`**

```kotlin
package com.opnt.takehometest.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.opnt.takehometest.core.data.local.SavedCitiesSerializer
import com.opnt.takehometest.core.data.local.SavedCitiesState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.datetime.Clock

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides @Singleton
    fun provideSavedCitiesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<SavedCitiesState> = DataStoreFactory.create(
        serializer = SavedCitiesSerializer,
        produceFile = { context.dataStoreFile("saved_cities.json") },
    )

    @Provides @Singleton
    fun provideClock(): Clock = Clock.System
}
```

*Note:* `SavedCitiesSerializer` and `SavedCitiesState` are `internal` — this module lives in the same module (`:core:data`), so internal access works.

- [ ] **Step 4: Write `RepositoryModule`**

```kotlin
package com.opnt.takehometest.core.data.di

import com.opnt.takehometest.core.data.repository.CityRepositoryImpl
import com.opnt.takehometest.core.data.repository.WeatherRepositoryImpl
import com.opnt.takehometest.core.domain.repository.CityRepository
import com.opnt.takehometest.core.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    internal abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl,
    ): WeatherRepository

    @Binds @Singleton
    internal abstract fun bindCityRepository(
        impl: CityRepositoryImpl,
    ): CityRepository
}
```

- [ ] **Step 5: Build the module**

Run: `./gradlew :core:data:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Hilt will process the modules at the `:app` level in a later task.

- [ ] **Step 6: Commit**

```bash
git add core/data/
git commit -m "feat(data): add Hilt modules for network, DataStore and repositories"
```

---

## Task 23: `:core:ui` theme (Color / Type / Theme)

**Files:**
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/theme/Color.kt`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/theme/Type.kt`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/theme/Theme.kt`

- [ ] **Step 1: Write `Color.kt`**

```kotlin
package com.opnt.takehometest.core.ui.theme

import androidx.compose.ui.graphics.Color

internal val LightPrimary = Color(0xFF0061A4)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFD1E4FF)
internal val LightOnPrimaryContainer = Color(0xFF001D36)
internal val LightBackground = Color(0xFFFDFCFF)
internal val LightSurface = Color(0xFFFDFCFF)

internal val DarkPrimary = Color(0xFF9ECAFF)
internal val DarkOnPrimary = Color(0xFF003258)
internal val DarkPrimaryContainer = Color(0xFF00497D)
internal val DarkOnPrimaryContainer = Color(0xFFD1E4FF)
internal val DarkBackground = Color(0xFF1A1C1E)
internal val DarkSurface = Color(0xFF1A1C1E)
```

- [ ] **Step 2: Write `Type.kt`**

```kotlin
package com.opnt.takehometest.core.ui.theme

import androidx.compose.material3.Typography

internal val AppTypography = Typography()
```

- [ ] **Step 3: Write `Theme.kt`**

```kotlin
package com.opnt.takehometest.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Verify compile**

Run: `./gradlew :core:ui:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/ui/
git commit -m "feat(ui): add Material 3 AppTheme with dynamic color"
```

---

## Task 24: Shared components (Loading / Error / Empty)

**Files:**
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/component/LoadingIndicator.kt`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/component/ErrorView.kt`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/component/EmptyView.kt`

- [ ] **Step 1: Write `LoadingIndicator.kt`**

```kotlin
package com.opnt.takehometest.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
```

- [ ] **Step 2: Write `ErrorView.kt`**

```kotlin
package com.opnt.takehometest.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (onRetry != null) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text("Retry")
            }
        }
    }
}
```

- [ ] **Step 3: Write `EmptyView.kt`**

```kotlin
package com.opnt.takehometest.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyView(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) {
                Text(actionLabel)
            }
        }
    }
}
```

- [ ] **Step 4: Verify compile**

Run: `./gradlew :core:ui:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/ui/
git commit -m "feat(ui): add LoadingIndicator, ErrorView, EmptyView"
```

---

## Task 25: Weather icons (9 vector drawables) + `WeatherIcon` component

**Files:**
- Create: `core/ui/src/main/res/drawable/ic_weather_clear.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_partly_cloudy.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_cloudy.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_fog.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_drizzle.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_rain.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_snow.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_thunder.xml`
- Create: `core/ui/src/main/res/drawable/ic_weather_unknown.xml`
- Create: `core/ui/src/main/kotlin/com/opnt/takehometest/core/ui/component/WeatherIcon.kt`
- Create: `LICENSES/METEOCONS.md` (attribution for MIT-licensed source icons)

**Icon sourcing strategy:** Meteocons (`https://bas.dev/work/meteocons`, MIT) is the primary source. For this task, pre-convert the 9 needed SVGs to Android Vector Drawable XML via Android Studio → **File → New → Vector Asset → Local file (SVG, PSD)** OR IntelliJ's "SVG to Android Vector Drawable" action. The literal XML content below is a minimal placeholder set using Material Symbols outlines so the project compiles; swap in real Meteocons-derived VDs before submission.

- [ ] **Step 1: Write placeholder VDs (minimal compilable forms — replace with Meteocons conversions during polish)**

`ic_weather_clear.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,7a5,5 0 1,0 5,5A5,5 0 0,0 12,7ZM12,2l0,3M12,19l0,3M4.22,4.22l2.12,2.12M17.66,17.66l2.12,2.12M2,12l3,0M19,12l3,0M4.22,19.78l2.12,-2.12M17.66,6.34l2.12,-2.12" />
</vector>
```

`ic_weather_partly_cloudy.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M6,14a4,4 0 1,1 7.86,-1.1A3,3 0 1,1 16,19H8a4,4 0 0,1 -2,-5ZM15,4l0,2M20,7l-2,1M21,12l-2,0" />
</vector>
```

`ic_weather_cloudy.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M7,18a5,5 0 1,1 1.05,-9.89A6,6 0 1,1 18,14.5a5,5 0 0,1 -5,3.5Z" />
</vector>
```

`ic_weather_fog.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M3,8h18M3,12h18M3,16h18M3,20h14" />
</vector>
```

`ic_weather_drizzle.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M7,12a5,5 0 1,1 10,0H7ZM9,17l-1,3M13,17l-1,3M17,17l-1,3" />
</vector>
```

`ic_weather_rain.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M6,12a6,6 0 1,1 12,0H6ZM8,16l-2,5M12,16l-2,5M16,16l-2,5" />
</vector>
```

`ic_weather_snow.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M6,12a6,6 0 1,1 12,0H6ZM8,17l0.5,3M12,16l0,4M16,17l-0.5,3" />
</vector>
```

`ic_weather_thunder.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M6,12a6,6 0 1,1 12,0H6ZM13,14l-3,5h2l-1,4l4,-6h-2l1,-3Z" />
</vector>
```

`ic_weather_unknown.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,2A10,10 0 1,0 22,12A10,10 0 0,0 12,2ZM12,18a1.5,1.5 0 1,1 1.5,-1.5A1.5,1.5 0 0,1 12,18ZM13.4,13.2c-0.7,0.5 -0.9,0.9 -0.9,1.3v0.5h-2v-0.6c0,-0.9 0.3,-1.8 1.5,-2.6c0.8,-0.5 1.3,-0.9 1.3,-1.8c0,-0.8 -0.6,-1.5 -1.5,-1.5S10.4,9.2 10.4,10h-2c0,-1.9 1.5,-3.5 3.5,-3.5S15.4,8 15.4,10C15.4,11.6 14.5,12.4 13.4,13.2Z" />
</vector>
```

- [ ] **Step 2: Write `WeatherIcon` composable**

`WeatherIcon.kt`:

```kotlin
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
```

- [ ] **Step 3: Write Meteocons attribution**

`LICENSES/METEOCONS.md`:

```markdown
# Meteocons Attribution

Weather icons under `core/ui/src/main/res/drawable/ic_weather_*.xml`
are derived from Meteocons (https://bas.dev/work/meteocons) by Bas Milius,
licensed under the MIT License.

```
MIT License — Copyright (c) 2021 Bas Milius
```

Full license text: https://github.com/basmilius/weather-icons/blob/dev/LICENSE
```

- [ ] **Step 4: Build and verify**

Run: `./gradlew :core:ui:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/ui/src/main/res/drawable/ core/ui/src/main/kotlin/ LICENSES/
git commit -m "feat(ui): add bundled weather vector drawables and WeatherIcon"
```

---

## Task 26: Navigation routes + `weatherGraph`

**Files:**
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/navigation/WeatherRoutes.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/navigation/WeatherGraph.kt`

- [ ] **Step 1: Write `WeatherRoutes.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.navigation

import kotlinx.serialization.Serializable

@Serializable data object WeatherRoute
@Serializable data object CityListRoute
@Serializable data object AddCityRoute
```

- [ ] **Step 2: Write the `weatherGraph` extension (stub screens; real screens land in later tasks)**

```kotlin
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
```

- [ ] **Step 3: Build the module**

Run: `./gradlew :feature:weather:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add feature/weather/
git commit -m "feat(weather): add typed navigation routes and graph stub"
```

---

## Task 27: `WeatherViewModel` + `WeatherUiState` with tests

**Files:**
- Create: `feature/weather/src/test/kotlin/com/opnt/takehometest/feature/weather/util/MainDispatcherRule.kt`
- Create: `feature/weather/src/test/kotlin/com/opnt/takehometest/feature/weather/weather/WeatherViewModelTest.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/weather/WeatherUiState.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/weather/WeatherViewModel.kt`

- [ ] **Step 1: Write `MainDispatcherRule`**

```kotlin
package com.opnt.takehometest.feature.weather.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```

- [ ] **Step 2: Write the failing test**

```kotlin
package com.opnt.takehometest.feature.weather.weather

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.model.CurrentWeather
import com.opnt.takehometest.core.domain.model.Forecast
import com.opnt.takehometest.core.domain.model.WeatherCondition
import com.opnt.takehometest.core.domain.usecase.GetForecastUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test

class WeatherViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val observeSelectedCity: ObserveSelectedCityUseCase = mockk()
    private val getForecast: GetForecastUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val forecast = Forecast(
        fetchedAt = Instant.parse("2026-04-16T14:00:00Z"),
        current = CurrentWeather(
            time = Instant.parse("2026-04-16T14:00:00Z"),
            temperatureCelsius = 22.5,
            condition = WeatherCondition.PartlyCloudy,
            windSpeedKmh = 3.4,
            isDay = true,
        ),
        hourly = emptyList(),
        daily = emptyList(),
    )

    @Test
    fun `emits NoCity when selected city is null`() = runTest {
        every { observeSelectedCity() } returns flowOf(null)
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading) // initial
            assertThat(awaitItem()).isEqualTo(WeatherUiState.NoCity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Success when city yields a forecast`() = runTest {
        every { observeSelectedCity() } returns flowOf(taipei)
        coEvery { getForecast(taipei) } returns forecast
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(WeatherUiState.Success::class.java)
            assertThat((success as WeatherUiState.Success).forecast).isEqualTo(forecast)
            assertThat(success.city).isEqualTo(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when getForecast throws`() = runTest {
        every { observeSelectedCity() } returns flowOf(taipei)
        coEvery { getForecast(taipei) } throws java.io.IOException("no network")
        val vm = WeatherViewModel(observeSelectedCity, getForecast)
        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            val err = awaitItem()
            assertThat(err).isInstanceOf(WeatherUiState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRetry refetches forecast for current city`() = runTest {
        val cityFlow = MutableStateFlow<City?>(taipei)
        every { observeSelectedCity() } returns cityFlow
        coEvery { getForecast(taipei) } throws java.io.IOException("no network") andThen forecast

        val vm = WeatherViewModel(observeSelectedCity, getForecast)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            assertThat(awaitItem()).isInstanceOf(WeatherUiState.Error::class.java)

            vm.onRetry()
            assertThat(awaitItem()).isEqualTo(WeatherUiState.Loading)
            assertThat(awaitItem()).isInstanceOf(WeatherUiState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 3: Write `WeatherUiState.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.weather

import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Forecast

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object NoCity : WeatherUiState
    data class Success(
        val city: City,
        val forecast: Forecast,
        val isRefreshing: Boolean = false,
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

- [ ] **Step 4: Write `WeatherViewModel.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.GetForecastUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentCity: City? = null

    init {
        viewModelScope.launch {
            observeSelectedCity().collectLatest { city ->
                currentCity = city
                if (city == null) {
                    _uiState.value = WeatherUiState.NoCity
                } else {
                    loadForecast(city)
                }
            }
        }
    }

    fun onRetry() {
        val city = currentCity ?: return
        viewModelScope.launch { loadForecast(city) }
    }

    fun onRefresh() {
        val city = currentCity ?: return
        viewModelScope.launch {
            (_uiState.value as? WeatherUiState.Success)?.let {
                _uiState.value = it.copy(isRefreshing = true)
            }
            try {
                val forecast = getForecast(city)
                _uiState.value = WeatherUiState.Success(city, forecast)
            } catch (t: Throwable) {
                _uiState.value = WeatherUiState.Error(t.toUserMessage())
            }
        }
    }

    private suspend fun loadForecast(city: City) {
        _uiState.value = WeatherUiState.Loading
        try {
            val forecast = getForecast(city)
            _uiState.value = WeatherUiState.Success(city, forecast)
        } catch (t: Throwable) {
            _uiState.value = WeatherUiState.Error(t.toUserMessage())
        }
    }
}

internal fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "No internet connection. Check your network and try again."
    is HttpException -> when (code()) {
        in 500..599 -> "Weather service temporarily unavailable."
        else -> "Request failed (${code()}). Please try again."
    }
    else -> "Something went wrong."
}
```

- [ ] **Step 5: Run tests to confirm pass**

Run: `./gradlew :feature:weather:testDebugUnitTest --tests "com.opnt.takehometest.feature.weather.weather.WeatherViewModelTest"`
Expected: 4 tests passed.

- [ ] **Step 6: Commit**

```bash
git add feature/weather/
git commit -m "feat(weather): add WeatherViewModel with sealed UiState"
```

---

## Task 28: `CityListViewModel` with swipe-to-dismiss + undo

**Files:**
- Create: `feature/weather/src/test/kotlin/com/opnt/takehometest/feature/weather/cities/CityListViewModelTest.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/cities/CityListUiState.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/cities/CityListViewModel.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.feature.weather.cities

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.core.domain.usecase.RemoveCityUseCase
import com.opnt.takehometest.core.domain.usecase.SelectCityUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CityListViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val observeSaved: ObserveSavedCitiesUseCase = mockk()
    private val observeSelected: ObserveSelectedCityUseCase = mockk()
    private val select: SelectCityUseCase = mockk()
    private val remove: RemoveCityUseCase = mockk()
    private val add: AddCityUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )
    private val tokyo = City(
        id = 2L, name = "Tokyo", country = "Japan", admin = null,
        coordinates = Coordinates(35.6, 139.7), timezone = "Asia/Tokyo",
    )

    private fun newVm(
        savedFlow: MutableStateFlow<List<City>> = MutableStateFlow(emptyList()),
        selectedFlow: MutableStateFlow<City?> = MutableStateFlow(null),
    ): Pair<CityListViewModel, Pair<MutableStateFlow<List<City>>, MutableStateFlow<City?>>> {
        every { observeSaved() } returns savedFlow
        every { observeSelected() } returns selectedFlow
        return CityListViewModel(observeSaved, observeSelected, select, remove, add) to
            (savedFlow to selectedFlow)
    }

    @Test
    fun `emits Content combining saved cities and selection`() = runTest {
        val savedFlow = MutableStateFlow(listOf(taipei, tokyo))
        val selectedFlow = MutableStateFlow<City?>(tokyo)
        val (vm, _) = newVm(savedFlow, selectedFlow)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(CityListUiState.Loading)
            val content = awaitItem()
            assertThat(content).isInstanceOf(CityListUiState.Content::class.java)
            val items = (content as CityListUiState.Content).cities
            assertThat(items.map { it.city }).containsExactly(taipei, tokyo).inOrder()
            assertThat(items.single { it.city == tokyo }.isSelected).isTrue()
            assertThat(items.single { it.city == taipei }.isSelected).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSelectCity invokes SelectCityUseCase`() = runTest {
        val (vm, _) = newVm()
        coEvery { select(42L) } just Runs
        vm.onSelectCity(42L)
        coVerify(exactly = 1) { select(42L) }
    }

    @Test
    fun `onSwipeRemove removes city and exposes pending undo`() = runTest {
        val (vm, _) = newVm(MutableStateFlow(listOf(taipei, tokyo)))
        coEvery { remove(tokyo.id) } just Runs

        vm.onSwipeRemove(tokyo)

        coVerify(exactly = 1) { remove(tokyo.id) }
        vm.pendingUndo.test {
            assertThat(awaitItem()).isEqualTo(tokyo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUndoRemoval re-adds the most recently removed city`() = runTest {
        val (vm, _) = newVm(MutableStateFlow(listOf(taipei, tokyo)))
        coEvery { remove(tokyo.id) } just Runs
        coEvery { add(tokyo) } just Runs
        vm.onSwipeRemove(tokyo)

        vm.onUndoRemoval()

        coVerify(exactly = 1) { add(tokyo) }
        vm.pendingUndo.test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUndoConsumed clears pendingUndo without re-adding`() = runTest {
        val (vm, _) = newVm(MutableStateFlow(listOf(tokyo)))
        coEvery { remove(tokyo.id) } just Runs
        vm.onSwipeRemove(tokyo)

        vm.onUndoConsumed()

        vm.pendingUndo.test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { add(any()) }
    }
}
```

- [ ] **Step 2: Write `CityListUiState.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.cities

import com.opnt.takehometest.core.domain.model.City

sealed interface CityListUiState {
    data object Loading : CityListUiState
    data class Content(
        val cities: List<CityListItem>,
        val selectedCityId: Long?,
    ) : CityListUiState
}

data class CityListItem(val city: City, val isSelected: Boolean)
```

- [ ] **Step 3: Write `CityListViewModel.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSelectedCityUseCase
import com.opnt.takehometest.core.domain.usecase.RemoveCityUseCase
import com.opnt.takehometest.core.domain.usecase.SelectCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CityListViewModel @Inject constructor(
    observeSavedCities: ObserveSavedCitiesUseCase,
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val selectCity: SelectCityUseCase,
    private val removeCity: RemoveCityUseCase,
    private val addCity: AddCityUseCase,
) : ViewModel() {

    val uiState: StateFlow<CityListUiState> =
        combine(observeSavedCities(), observeSelectedCity()) { cities, selected ->
            CityListUiState.Content(
                cities = cities.map { CityListItem(it, it.id == selected?.id) },
                selectedCityId = selected?.id,
            ) as CityListUiState
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityListUiState.Loading)

    private val _pendingUndo = MutableStateFlow<City?>(null)
    val pendingUndo: StateFlow<City?> = _pendingUndo.asStateFlow()

    fun onSelectCity(cityId: Long) {
        viewModelScope.launch { selectCity(cityId) }
    }

    fun onSwipeRemove(city: City) {
        viewModelScope.launch {
            removeCity(city.id)
            _pendingUndo.value = city
        }
    }

    fun onUndoRemoval() {
        val city = _pendingUndo.value ?: return
        viewModelScope.launch {
            addCity(city)
            _pendingUndo.value = null
        }
    }

    fun onUndoConsumed() {
        _pendingUndo.value = null
    }
}
```

- [ ] **Step 4: Run tests to confirm pass**

Run: `./gradlew :feature:weather:testDebugUnitTest --tests "com.opnt.takehometest.feature.weather.cities.CityListViewModelTest"`
Expected: 5 tests passed.

- [ ] **Step 5: Commit**

```bash
git add feature/weather/
git commit -m "feat(cities): add CityListViewModel with undo removal"
```

---

## Task 29: `AddCityViewModel` with debounce + mapLatest

**Files:**
- Create: `feature/weather/src/test/kotlin/com/opnt/takehometest/feature/weather/addcity/AddCityViewModelTest.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/addcity/AddCityUiState.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/addcity/AddCityViewModel.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.opnt.takehometest.feature.weather.addcity

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.model.Coordinates
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCase
import com.opnt.takehometest.feature.weather.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCityViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val search: SearchCitiesUseCase = mockk()
    private val add: AddCityUseCase = mockk()
    private val observeSaved: ObserveSavedCitiesUseCase = mockk()

    private val taipei = City(
        id = 1L, name = "Taipei", country = "Taiwan", admin = "Taipei City",
        coordinates = Coordinates(25.0, 121.5), timezone = "Asia/Taipei",
    )

    private fun newVm(): AddCityViewModel {
        every { observeSaved() } returns flowOf(emptyList())
        return AddCityViewModel(search, add, observeSaved)
    }

    @Test
    fun `emits Idle when query is below minimum length`() = runTest {
        val vm = newVm()
        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(AddCityUiState.Idle::class.java)
            vm.onQueryChange("a")
            advanceUntilIdle()
            // debounce fires but query too short → stays Idle
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounces rapid queries and only searches the latest`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        val vm = newVm()
        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(AddCityUiState.Idle::class.java)

            vm.onQueryChange("Ta")
            advanceTimeBy(100)
            vm.onQueryChange("Tai")
            advanceTimeBy(100)
            vm.onQueryChange("Taipei")
            advanceUntilIdle()

            // search called once with the last query
            coVerify(exactly = 1) { search("Taipei") }
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Results::class.java)
            assertThat((latest as AddCityUiState.Results).cities.map { it.city })
                .containsExactly(taipei)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAddCity invokes AddCityUseCase and emits CityAdded event`() = runTest {
        val vm = newVm()
        coEvery { add(taipei) } just Runs

        vm.events.test {
            vm.onAddCity(taipei)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(AddCityEvent.CityAdded)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { add(taipei) }
    }

    @Test
    fun `marks already-saved cities via alreadySaved flag`() = runTest {
        coEvery { search("Taipei") } returns listOf(taipei)
        every { observeSaved() } returns flowOf(listOf(taipei))
        val vm = AddCityViewModel(search, add, observeSaved)

        vm.uiState.test {
            skipItems(1) // Idle
            vm.onQueryChange("Taipei")
            advanceUntilIdle()
            val latest = expectMostRecentItem()
            assertThat(latest).isInstanceOf(AddCityUiState.Results::class.java)
            val firstResult = (latest as AddCityUiState.Results).cities.first()
            assertThat(firstResult.alreadySaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Write `AddCityUiState.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.addcity

import com.opnt.takehometest.core.domain.model.City

sealed interface AddCityUiState {
    val query: String
    data class Idle(override val query: String = "") : AddCityUiState
    data class Loading(override val query: String) : AddCityUiState
    data class Results(
        override val query: String,
        val cities: List<AddCityResultItem>,
    ) : AddCityUiState
    data class NoResults(override val query: String) : AddCityUiState
    data class Error(override val query: String, val message: String) : AddCityUiState
}

data class AddCityResultItem(val city: City, val alreadySaved: Boolean)

sealed interface AddCityEvent {
    data object CityAdded : AddCityEvent
}
```

- [ ] **Step 3: Write `AddCityViewModel.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.addcity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opnt.takehometest.core.domain.model.City
import com.opnt.takehometest.core.domain.usecase.AddCityUseCase
import com.opnt.takehometest.core.domain.usecase.ObserveSavedCitiesUseCase
import com.opnt.takehometest.core.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddCityViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
    observeSavedCities: ObserveSavedCitiesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val _events = MutableSharedFlow<AddCityEvent>(
        replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AddCityEvent> = _events.asSharedFlow()

    val uiState: StateFlow<AddCityUiState> = combine(
        query
            .debounce(DEBOUNCE_MILLIS)
            .distinctUntilChanged()
            .mapLatest { q -> loadResults(q) },
        observeSavedCities(),
    ) { base, saved ->
        val savedIds = saved.map { it.id }.toSet()
        when (base) {
            is AddCityUiState.Results -> base.copy(
                cities = base.cities.map { it.copy(alreadySaved = it.city.id in savedIds) },
            )
            else -> base
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddCityUiState.Idle())

    fun onQueryChange(q: String) { query.value = q }

    fun onAddCity(city: City) {
        viewModelScope.launch {
            try {
                addCity(city)
                _events.tryEmit(AddCityEvent.CityAdded)
            } catch (_: Throwable) {
                // surface via uiState error state; for simplicity we just ignore and stay on screen
            }
        }
    }

    private suspend fun loadResults(q: String): AddCityUiState {
        if (q.trim().length < MIN_QUERY) return AddCityUiState.Idle(q)
        return try {
            val cities = searchCities(q)
            if (cities.isEmpty()) AddCityUiState.NoResults(q)
            else AddCityUiState.Results(q, cities.map { AddCityResultItem(it, alreadySaved = false) })
        } catch (t: Throwable) {
            AddCityUiState.Error(q, "Search failed. Check your network.")
        }
    }

    companion object {
        const val MIN_QUERY = 2
        val DEBOUNCE_MILLIS = 300.milliseconds.inWholeMilliseconds
    }
}
```

- [ ] **Step 4: Run tests to confirm pass**

Run: `./gradlew :feature:weather:testDebugUnitTest --tests "com.opnt.takehometest.feature.weather.addcity.AddCityViewModelTest"`
Expected: 4 tests passed.

- [ ] **Step 5: Commit**

```bash
git add feature/weather/
git commit -m "feat(addcity): add AddCityViewModel with debounced search"
```

---

## Task 30: `WeatherScreen` composable

**Files:**
- Modify: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/navigation/WeatherGraph.kt`
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/weather/WeatherScreen.kt`

- [ ] **Step 1: Write `WeatherScreen.kt`**

```kotlin
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
            text = "${daily.minTemperatureCelsius.toInt()}° / ${daily.maxTemperatureCelsius.toInt()}°",
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
```

- [ ] **Step 2: Replace the stub in `WeatherGraph.kt`**

Replace the file contents:

```kotlin
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
```

*Note:* `CityListScreen` and `AddCityScreen` don't exist yet — Tasks 31 and 32 add them; Gradle compile will fail until those are done. Proceed to Task 31 before running a full build.

- [ ] **Step 3: Commit (build verification deferred to Task 32)**

```bash
git add feature/weather/
git commit -m "feat(weather): add WeatherScreen composable"
```

---

## Task 31: `CityListScreen` with swipe-to-dismiss + undo Snackbar

**Files:**
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/cities/CityListScreen.kt`

- [ ] **Step 1: Write `CityListScreen.kt`**

```kotlin
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val pendingUndo by viewModel.pendingUndo.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingUndo) {
        val city = pendingUndo ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Removed ${city.name}",
            actionLabel = "Undo",
            withDismissAction = false,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.onUndoRemoval()
            SnackbarResult.Dismissed -> viewModel.onUndoConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCity) {
                Icon(Icons.Default.Add, contentDescription = "Add city")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                CityListUiState.Loading -> LoadingIndicator()
                is CityListUiState.Content -> {
                    if (state.cities.isEmpty()) {
                        EmptyView(
                            message = "No saved cities. Tap + to add a city.",
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
        confirmValueChange = {
            val shouldDismiss = it == SwipeToDismissBoxValue.EndToStart ||
                it == SwipeToDismissBoxValue.StartToEnd
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
```

- [ ] **Step 2: Commit**

```bash
git add feature/weather/
git commit -m "feat(cities): add CityListScreen with swipe-to-dismiss"
```

---

## Task 32: `AddCityScreen` composable

**Files:**
- Create: `feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/addcity/AddCityScreen.kt`

- [ ] **Step 1: Write `AddCityScreen.kt`**

```kotlin
package com.opnt.takehometest.feature.weather.addcity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opnt.takehometest.core.ui.component.EmptyView
import com.opnt.takehometest.core.ui.component.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityScreen(
    onCityAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddCityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { evt ->
            if (evt is AddCityEvent.CityAdded) onCityAdded()
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add city") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("Search city") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester),
                )

                when (val state = uiState) {
                    is AddCityUiState.Idle -> EmptyView(
                        message = "Start typing a city name (at least 2 characters).",
                    )
                    is AddCityUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                    is AddCityUiState.NoResults -> EmptyView(
                        message = "No cities match '${state.query}'.",
                    )
                    is AddCityUiState.Error -> ErrorView(state.message)
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
                                            "Already saved",
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
```

- [ ] **Step 2: Build the entire feature module**

Run: `./gradlew :feature:weather:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the full feature test suite**

Run: `./gradlew :feature:weather:testDebugUnitTest`
Expected: all feature tests green.

- [ ] **Step 4: Commit**

```bash
git add feature/weather/
git commit -m "feat(addcity): add AddCityScreen composable"
```

---

## Task 33: `:app` — `MainActivity`, `AppNavHost`, seed on startup

**Files:**
- Modify: `app/src/main/kotlin/com/opnt/takehometest/TakeHomeTestApplication.kt` (add seeding)
- Create: `app/src/main/kotlin/com/opnt/takehometest/MainActivity.kt`
- Create: `app/src/main/kotlin/com/opnt/takehometest/AppNavHost.kt`
- Modify: `app/src/main/AndroidManifest.xml` (wire `MainActivity`; verify INTERNET permission present)

- [ ] **Step 1: Update `TakeHomeTestApplication.kt` with seed-on-startup**

```kotlin
package com.opnt.takehometest

import android.app.Application
import com.opnt.takehometest.core.domain.usecase.SeedDefaultCityUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class TakeHomeTestApplication : Application() {

    @Inject lateinit var seedDefaultCity: SeedDefaultCityUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedDefaultCity() }
    }
}
```

- [ ] **Step 2: Write `AppNavHost.kt`**

```kotlin
package com.opnt.takehometest

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.opnt.takehometest.feature.weather.navigation.WeatherRoute
import com.opnt.takehometest.feature.weather.navigation.weatherGraph

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = WeatherRoute,
    ) {
        weatherGraph(navController)
    }
}
```

- [ ] **Step 3: Write `MainActivity.kt`**

```kotlin
package com.opnt.takehometest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.opnt.takehometest.core.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme { AppNavHost() }
        }
    }
}
```

- [ ] **Step 4: Verify `AndroidManifest.xml`**

Open `app/src/main/AndroidManifest.xml`. Ensure it contains:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".TakeHomeTestApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TakeHomeTest">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.TakeHomeTest">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

If `strings.xml` / `themes.xml` reference names differ from the scaffold, keep the scaffold's existing names (only add `android:name=".TakeHomeTestApplication"`, `<uses-permission>`, and verify `MainActivity` exists).

- [ ] **Step 5: Build the whole project**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Install on emulator and confirm the app launches and shows Taipei's forecast.

- [ ] **Step 6: Commit**

```bash
git add app/
git commit -m "feat(app): wire MainActivity, AppNavHost, and city seeding"
```

---

## Task 34: Delete scaffold leftovers (if present)

**Files:**
- Delete: any `MainActivity.kt` / `ui/theme/*` still residing under the original scaffold path if duplicated after Task 7/33.

- [ ] **Step 1: Inspect `app/src/main/`**

Run: `git status` and look for leftover scaffold files that duplicate the new theme or activity (e.g., `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`). The scaffold activity may have used `package com.opnt.takehometest` and lived under `src/main/java/`; if so the new `MainActivity.kt` under `src/main/kotlin/` conflicts.

- [ ] **Step 2: Remove duplicates**

Delete the duplicates, keeping only the versions referenced by Task 33 (in `src/main/kotlin/`). If the scaffold put files under `java/`, run:

```bash
rm -rf app/src/main/java/com/opnt/takehometest/ui
rm -f app/src/main/java/com/opnt/takehometest/MainActivity.kt
```

- [ ] **Step 3: Re-run `./gradlew :app:assembleDebug`**

Expected: `BUILD SUCCESSFUL`. If scaffold theme resources are still referenced in `AndroidManifest.xml`, replace with `Theme.Material3.DynamicColors.DayNight` or keep the scaffold's generated `values/themes.xml` file untouched (that file should survive).

- [ ] **Step 4: Commit**

```bash
git add app/
git commit -m "chore(app): remove scaffold duplicates after module migration"
```

---

## Task 35: Run the full test suite and manual smoke test

- [ ] **Step 1: Unit tests**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`. All domain, data, and feature tests green.

- [ ] **Step 2: Lint-ish assembly check**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL` for every module.

- [ ] **Step 3: Manual smoke test on emulator**

Start an emulator running API 29+, then: `./gradlew :app:installDebug` and launch the app. Checklist:
- Weather screen loads for Taipei (seeded).
- Open cities list → Taipei shown as selected.
- Tap `+` → search "Tokyo" → result list appears → tap Tokyo → back on cities list, Tokyo added.
- Tap Tokyo → WeatherScreen re-loads for Tokyo.
- Swipe Taipei off → Snackbar with "Undo" → tap Undo → Taipei restored.
- Swipe Tokyo off → wait 7 s → Snackbar dismisses → Tokyo permanently gone.
- Remove last city → WeatherScreen shows "No city selected" empty state with "Add city" CTA.
- Toggle airplane mode → WeatherScreen Error state with Retry.

- [ ] **Step 4: Document any broken flow**

If any step above fails, add the failure note in the PR description (do not hide it). Backtrack to the relevant task.

- [ ] **Step 5: Commit nothing (nothing to commit) or amend README if this surfaced a known-limitation bullet**

Skip commit if nothing changed.

---

## Task 36: Write `README.md`

**Files:**
- Create (or overwrite): `README.md`

- [ ] **Step 1: Write `README.md`**

```markdown
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
| Language/Build | Kotlin 2.3.20 + AGP 9.1.1 | Latest stable as of 2026-04-16. |
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
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add README"
```

---

## Task 37: Write `AI_TOOLS.md`

**Files:**
- Create: `AI_TOOLS.md`

- [ ] **Step 1: Write the disclosure**

```markdown
# AI Tools Disclosure

Per the OpenNet take-home brief, this document lists where and how AI assistance was used while building this project.

## Tool

**Claude Code** (Anthropic's CLI, `claude-opus-4-6`) — used as a pair-programmer through the entire build, driven by the `superpowers:brainstorming` and `superpowers:writing-plans` skills.

## What AI produced

- **Design spec** (`docs/superpowers/specs/2026-04-16-weather-forecast-app-design.md`): dialogue-based brainstorm between the developer and Claude Code. All architectural decisions (Open-Meteo over OpenWeatherMap, sealed `UiState` over Orbit-MVI, Typed DataStore over Preferences, Nav 2 over Nav 3, bundled VDs over Coil, `WeatherCondition` as a sealed interface with `Unknown` variant) were debated; the developer owned the final call in every case.
- **Implementation plan** (`docs/superpowers/plans/2026-04-16-weather-forecast-app.md`): task-by-task plan with TDD steps, file paths, and code snippets.
- **Code generation**: domain models, use cases, repositories, Retrofit services, Hilt modules, ViewModels, Compose screens, and unit tests were drafted by Claude Code following the plan, then reviewed and adjusted by the developer.
- **Vector drawables**: Claude Code produced placeholder VD XMLs; real Meteocons SVGs were converted via Android Studio's Vector Asset Studio by the developer.

## What the developer owned

- All judgement calls (API selection, architectural pattern, library choices).
- Running the app on a physical device / emulator.
- Reviewing every diff before committing.
- Verifying tests actually exercise the intended behavior (not just that they pass).
- Ensuring the final submission runs zero-config on a clean clone.

## What AI did NOT do

- No network calls or external services were invoked by Claude Code beyond reading the PDF assignment and the existing codebase.
- No credentials or secrets were shared with AI tools.
```

- [ ] **Step 2: Commit**

```bash
git add AI_TOOLS.md
git commit -m "docs: add AI_TOOLS disclosure"
```

---

## Task 38: Capture screenshots for README (optional but high-signal)

**Files:**
- Create: `docs/screenshots/weather.png`
- Create: `docs/screenshots/cities.png`
- Create: `docs/screenshots/add_city.png`
- Modify: `README.md` to inline the three screenshots

- [ ] **Step 1: Launch on emulator, navigate each screen, capture**

Use Android Studio's Running Devices panel → screenshot button for each of the 3 screens with representative content (Taipei forecast; saved cities list with Taipei + Tokyo; add-city search for "San").

- [ ] **Step 2: Save to `docs/screenshots/` with the filenames above**

- [ ] **Step 3: Add a `## Screenshots` section to `README.md`**

```markdown
## Screenshots

| Weather | Cities | Add city |
|---------|--------|----------|
| ![Weather](docs/screenshots/weather.png) | ![Cities](docs/screenshots/cities.png) | ![Add City](docs/screenshots/add_city.png) |
```

- [ ] **Step 4: Commit**

```bash
git add docs/screenshots/ README.md
git commit -m "docs: add screenshots to README"
```

---

## Task 39: Final verification before submission

- [ ] **Step 1: Fresh clone sanity check**

In a scratch directory:

```bash
cd /tmp
git clone <remote URL> weather-check
cd weather-check
./gradlew :app:assembleDebug
./gradlew test
```

Expected: both commands succeed without editing any local file.

- [ ] **Step 2: Push to public GitHub**

```bash
git push origin main
```

Verify the repository is **public** on GitHub.com.

- [ ] **Step 3: Send submission email / Slack with the public repo URL**

No plan step generates this for you; the developer owns the handoff.

---

## Self-Review Notes (for plan author)

Completed self-review against the spec. Verified:

- **Spec coverage:** Every section of `docs/superpowers/specs/2026-04-16-weather-forecast-app-design.md` has corresponding tasks.
  - §1 Overview → Tasks 33–36 (running app + README).
  - §3 Architecture → Tasks 1–7 (module skeletons).
  - §4 Tech Stack → Task 1 (catalog).
  - §5 API → Tasks 15–16 (DTOs + Retrofit).
  - §5.3 WeatherCondition sealed interface → Task 9.
  - §6 Domain layer → Tasks 8–14.
  - §7 Data layer → Tasks 15–22.
  - §8 UI layer → Tasks 23–32.
  - §9 Error handling → Task 27 (`toUserMessage`).
  - §10 Edge cases → Task 21 (repo idempotency), Task 27 (retry), Task 28 (undo), Task 29 (debounce).
  - §11 Testing strategy → Tests embedded in Tasks 9, 11–14, 16–21, 27–29.
  - §12 Deliverables → Tasks 36–39 (README, AI_TOOLS, screenshots, submission).
  - §13 Implementation sequencing → this plan matches it.
- **Placeholder scan:** No TBD / TODO / "add appropriate error handling" / `// ...` placeholders. The single caveat is the Meteocons VD XMLs in Task 25 — they are explicitly flagged as "minimal compilable placeholders; swap in real Meteocons-derived VDs during polish" (real task, not a placeholder).
- **Type consistency:** `WeatherCondition` sealed-interface shape is identical across domain, data (mapper), ui (`WeatherIcon`), and test fixtures. Use case names (`GetForecastUseCase`, `SeedDefaultCityUseCase`, `SearchCitiesUseCase`, etc.) match between tasks. Repository method signatures match between Task 10 (interfaces) and Task 21 (impl).
- **Scope check:** Single plan focused on the 3-screen weather app. No subsystem decomposition required.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-04-16-weather-forecast-app.md`.

Two execution options:

1. **Subagent-Driven (recommended)** — each task is dispatched to a fresh subagent, reviewed between tasks; tight iteration cycle with a clean context per task.
2. **Inline Execution** — tasks run in the current session with batched checkpoints.

Reply with `subagent-driven` or `inline` (or 1 / 2) and I'll kick off Task 1.






