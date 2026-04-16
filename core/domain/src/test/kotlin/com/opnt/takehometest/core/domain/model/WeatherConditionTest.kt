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
