package io.ileukocyte.openweather.entities

/**
 * Information about the weather in the location weather is requested for
 *
 * @param id
 * A weather condition ID
 * @param main
 * The weather parameters provided by OpenWeatherMap API
 * @param description
 * A detailed description of the weather condition
 * @param icon
 * A weather icon ID provided by OpenWeatherMap API
 */
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)