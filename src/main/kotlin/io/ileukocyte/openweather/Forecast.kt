package io.ileukocyte.openweather

import io.ileukocyte.openweather.entities.*

/**
 * A [OpenWeatherApi] wrapper of the requested forecast
 *
 * @param api
 * The current [OpenWeatherApi] instance
 * @param location
 * The provided location data
 * @param cloudiness
 * The provided cloudiness data
 * @param coordinates
 * The provided coordinates data
 * @param humidity
 * The provided humidity data
 * @param pressure
 * The provided atmospheric pressure data
 * @param temperature
 * The provided temperature data
 * @param time
 * The provided time data
 * @param visibility
 * The provided visibility data
 * @param weather
 * The provided weather data
 * @param wind
 * The provided wind data
 */
data class Forecast(
    val api: OpenWeatherApi,
    val location: Location,
    val cloudiness: Cloudiness,
    val coordinates: Coordinates,
    val humidity: Humidity,
    val pressure: Pressure,
    val temperature: Temperature,
    val time: Time,
    val visibility: Visibility,
    val weather: Weather,
    val wind: Wind
)