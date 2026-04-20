package com.opnt.takehometest.feature.weather.cities

import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityListScreenSourceTest {

    @Test
    fun cityListScreen_usesTopBarTextButton_insteadOfFab() {
        val source = String(
            Files.readAllBytes(
                findRepoRoot().resolve(
                    "feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/cities/CityListScreen.kt"
                )
            ),
            StandardCharsets.UTF_8,
        )

        assertFalse(source.contains("FloatingActionButton"))
        assertTrue(source.contains("TextButton("))
        assertTrue(source.contains("Text(\"Add city\")"))
    }

    @Test
    fun cityListScreen_usesThresholdedSwipeDelete_withoutUndoSnackbar() {
        val source = String(
            Files.readAllBytes(
                findRepoRoot().resolve(
                    "feature/weather/src/main/kotlin/com/opnt/takehometest/feature/weather/cities/CityListScreen.kt"
                )
            ),
            StandardCharsets.UTF_8,
        )

        assertTrue(source.contains("positionalThreshold"))
        assertTrue(source.contains("totalDistance * 0.35f"))
        assertFalse(source.contains("SnackbarHost("))
        assertFalse(source.contains("pendingUndo"))
        assertFalse(source.contains("showSnackbar("))
    }

    private fun findRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate repository root from ${Path.of("").toAbsolutePath()}")
    }
}
