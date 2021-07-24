package io.ileukocyte.openweather.entities

/**
 * Information about the location weather is requested for
 *
 * @param name
 * The location's name
 * @param id
 * An OpenWeatherMap API's unique ID for the city
 * @param countryCode
 * The country's code *(e.g. "US" for the United States)*
 */
data class Location(
    val name: String,
    val id: Int,
    val countryCode: String
)