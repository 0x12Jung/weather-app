package com.opnt.takehometest

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeResourcesTest {

    @Test
    fun activityTheme_isNotLight_andDefinesDarkWindowBackground() {
        val themesXml = Files.readString(findRepoRoot().resolve("app/src/main/res/values/themes.xml"))

        assertFalse(themesXml.contains("android:Theme.Material.Light.NoActionBar"))
        assertTrue(themesXml.contains("android:windowBackground"))
        assertTrue(themesXml.contains("@color/window_background"))
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
