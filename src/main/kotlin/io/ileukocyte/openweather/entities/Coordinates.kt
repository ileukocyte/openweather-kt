package io.ileukocyte.openweather.entities

/**
 * Geographical coordinates of the location weather is requested for
 *
 * @param longitude
 * A longitude value
 * @param latitude
 * A latitude value
 */
data class Coordinates(
    val longitude: Float,
    val latitude: Float
)