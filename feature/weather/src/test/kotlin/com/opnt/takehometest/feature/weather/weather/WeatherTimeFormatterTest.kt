package com.opnt.takehometest.feature.weather.weather

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Test

class WeatherTimeFormatterTest {

    @Test
    fun `formatHourlyHour uses provided city timezone instead of system timezone`() {
        val instant = Instant.parse("2026-04-20T12:00:00Z")

        val taipeiHour = formatHourlyHour(instant, TimeZone.of("Asia/Taipei"))
        val londonHour = formatHourlyHour(instant, TimeZone.of("Europe/London"))

        assertThat(taipeiHour).isEqualTo("20:00")
        assertThat(londonHour).isEqualTo("13:00")
    }
}
