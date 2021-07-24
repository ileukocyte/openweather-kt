package io.ileukocyte.openweather.extensions

import io.ileukocyte.openweather.entities.Temperature
import io.ileukocyte.openweather.entities.Temperature.TemperatureUnit

private val Float.asCelcius get() = this * 1.8f + 32
private val Float.asFahrenheit get() = (this - 32) / 1.8f

fun Temperature.convertTo(unitToConvertTo: TemperatureUnit): Temperature =
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
                    convertTo(TemperatureUnit.CELSIUS_DEGREES).convertTo(TemperatureUnit.FAHRENHEIT_DEGREES)
            }
        TemperatureUnit.FAHRENHEIT_DEGREES ->
            when (unitToConvertTo) {
                TemperatureUnit.CELSIUS_DEGREES ->
                    copy(temperature.asFahrenheit, feelsLike.asFahrenheit, minTemperature.asFahrenheit, maxTemperature.asFahrenheit, unitToConvertTo)
                TemperatureUnit.KELVIN ->
                    convertTo(TemperatureUnit.CELSIUS_DEGREES).convertTo(TemperatureUnit.KELVIN)
                TemperatureUnit.FAHRENHEIT_DEGREES -> this
            }
    }