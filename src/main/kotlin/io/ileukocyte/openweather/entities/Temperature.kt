package io.ileukocyte.openweather.entities

import io.ileukocyte.openweather.Units

/**
 * Information about the temperature in the location weather is requested for
 *
 * @param temperature
 * A temperature value, *[unit]*
 * @param feelsLike
 * A temperature value that accounts for the human perception of weather, *[unit]*
 * @param minTemperature
 * The minimal currently observed temperature value, *[unit]*
 * @param maxTemperature
 * The maximal currently observed temperature value, *[unit]*
 * @param unit
 * A temperature unit value provided by OpenWeatherMap API
 */
data class Temperature(
    val temperature: Float,
    val feelsLike: Float,
    val minTemperature: Float,
    val maxTemperature: Float,
    val unit: TemperatureUnit
) {
    enum class TemperatureUnit(val symbol: String, val units: Units) {
        KELVIN("K", Units.DEFAULT),
        CELSIUS_DEGREES("\u00b0C", Units.METRIC),
        FAHRENHEIT_DEGREES("\u00b0F", Units.IMPERIAL)
    }
}