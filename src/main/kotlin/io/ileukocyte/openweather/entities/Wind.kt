package io.ileukocyte.openweather.entities

import io.ileukocyte.openweather.Units

/**
 * Information about the wind in the location weather is requested for
 *
 * @param speed
 * A wind speed value, *[unit]*
 * @param direction
 * A wind direction value, degrees
 * @param gust
 * A wind gust value, *[unit]*
 * @param unit
 * A speed/gust unit value provided by OpenWeatherMap API
 */
data class Wind(
    val speed: Float,
    val direction: Int?,
    val gust: Float?,
    val unit: WindUnit,
) {
    /**
     * A wind direction name from [the degree value][direction]
     */
    val directionName get() = when (direction) {
        in 0..25, in 336..360 -> "North"
        in 26..70 -> "Northeast"
        in 71..110 -> "East"
        in 111..155 -> "Southeast"
        in 156..200 -> "South"
        in 201..250 -> "Southwest"
        in 251..290 -> "West"
        in 291..335 -> "Northwest"
        else -> null
    }

    enum class WindUnit(val asString: String, vararg val units: Units) {
        METERS_PER_SECONDS("m/s", Units.METRIC, Units.DEFAULT),
        MILES_PER_HOUR("mph", Units.IMPERIAL),
    }
}