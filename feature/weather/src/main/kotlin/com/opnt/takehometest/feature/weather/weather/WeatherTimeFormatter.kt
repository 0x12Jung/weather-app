package com.opnt.takehometest.feature.weather.weather

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun formatHourlyHour(time: Instant, zone: TimeZone): String {
    val hour = time.toLocalDateTime(zone).hour
    return "%02d:00".format(hour)
}
