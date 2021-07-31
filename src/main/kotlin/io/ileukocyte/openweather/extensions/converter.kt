package io.ileukocyte.openweather.extensions

import io.ileukocyte.openweather.entities.Temperature
import io.ileukocyte.openweather.entities.Temperature.TemperatureUnit
import io.ileukocyte.openweather.entities.Wind
import io.ileukocyte.openweather.entities.Wind.WindUnit

private val Float.asCelcius get() = this * 1.8f + 32
private val Float.asFahrenheit get() = (this - 32) / 1.8f

fun Temperature.convertUnitsTo(unitToConvertTo: TemperatureUnit): Temperature =
    when (unit) {
        TemperatureUnit.CELSIUS_DEGREES ->
            when (unitToConvertTo) {
                TemperatureUnit.CELSIUS_DEGREES -> this
                TemperatureUnit.KELVIN ->
                    copy(temperature + 273.15f, feelsLike + 273.15f, minTemperature + 273.15f, maxTemperature + 273.15f, unitToConvertTo)
                TemperatureUnit.FAHRENHEIT_DEGREES ->
                    copy(temperature.asCelcius, feelsLike.asCelcius, minTemperature.asCelcius, maxTemperature.asCelcius, unitToConvertTo)
            }
        TemperatureUnit.KELVIN ->
            when (unitToConvertTo) {
                TemperatureUnit.KELVIN -> this
                TemperatureUnit.CELSIUS_DEGREES ->
                    copy(temperature - 273.15f, feelsLike - 273.15f, minTemperature - 273.15f, maxTemperature - 273.15f, unitToConvertTo)
                TemperatureUnit.FAHRENHEIT_DEGREES ->
                    convertUnitsTo(TemperatureUnit.CELSIUS_DEGREES).convertUnitsTo(TemperatureUnit.FAHRENHEIT_DEGREES)
            }
        TemperatureUnit.FAHRENHEIT_DEGREES ->
            when (unitToConvertTo) {
                TemperatureUnit.CELSIUS_DEGREES ->
                    copy(temperature.asFahrenheit, feelsLike.asFahrenheit, minTemperature.asFahrenheit, maxTemperature.asFahrenheit, unitToConvertTo)
                TemperatureUnit.KELVIN ->
                    convertUnitsTo(TemperatureUnit.CELSIUS_DEGREES).convertUnitsTo(TemperatureUnit.KELVIN)
                TemperatureUnit.FAHRENHEIT_DEGREES -> this
            }
    }

fun Wind.convertUnitsTo(unitToConvertTo: WindUnit): Wind =
    when (unit) {
        WindUnit.MILES_PER_HOUR ->
            if (unitToConvertTo == WindUnit.MILES_PER_HOUR) this else copy(speed * 0.44704f, direction, gust, unitToConvertTo)
        WindUnit.METERS_PER_SECONDS ->
            if (unitToConvertTo == WindUnit.METERS_PER_SECONDS) this else copy(speed / 0.44704f, direction, gust, unitToConvertTo)
    }