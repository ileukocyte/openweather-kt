package io.ileukocyte.openweather.entities

/**
 * Information about the atmospheric pressure in the location weather is requested for
 *
 * @param pressure
 * An atmospheric pressure value, *hPa*
 * @param seaLevelPressure
 * An atmospheric pressure value on the sea level, *hPa*
 * @param groundLevelPressure
 * An atmospheric pressure value on the ground level, *hPa*
 */
data class Pressure(
    val pressure: Float,
    val seaLevelPressure: Float?,
    val groundLevelPressure: Float?
)