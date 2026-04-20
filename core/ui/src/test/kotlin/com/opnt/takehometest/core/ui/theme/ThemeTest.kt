package com.opnt.takehometest.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
    @Test
    fun appColorScheme_returnsCustomDarkPalette() {
        val colorScheme = appColorScheme()

        assertEquals(DarkPrimary, colorScheme.primary)
        assertEquals(DarkOnPrimary, colorScheme.onPrimary)
        assertEquals(DarkPrimaryContainer, colorScheme.primaryContainer)
        assertEquals(DarkOnPrimaryContainer, colorScheme.onPrimaryContainer)
        assertEquals(DarkBackground, colorScheme.background)
        assertEquals(DarkSurface, colorScheme.surface)
    }
}
