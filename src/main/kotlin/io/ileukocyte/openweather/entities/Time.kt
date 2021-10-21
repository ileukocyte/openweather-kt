package io.ileukocyte.openweather.entities

import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.TimeZone

/**
 * Information about the time in the location weather is requested for
 *
 * @param timeZone
 * The location's GMT time zone value
 * @param sunrise
 * A sunrise date value
 * @param sunset
 * A sunset date value
 */
data class Time(
    val timeZone: TimeZone,
    val sunrise: Date,
    val sunset: Date,
) {
    internal constructor(
        timeOffset: Int,
        sunrise: Date,
        sunset: Date,
    ) : this(
        TimeZone.getTimeZone(ZoneId.ofOffset("GMT", ZoneOffset.ofTotalSeconds(timeOffset))),
        sunrise,
        sunset,
    )
}