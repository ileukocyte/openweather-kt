@file:[JvmName("OpenWeatherKt") Suppress("UNUSED")]
package io.ileukocyte.openweather

import io.ileukocyte.openweather.entities.*
import io.ileukocyte.openweather.extensions.internal.getFloatOrNull
import io.ileukocyte.openweather.extensions.internal.getIntOrNull
import io.ileukocyte.openweather.extensions.internal.toJSONObject

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.URLEncoder
import java.util.Date
import javax.security.auth.login.LoginException

/**
 * The main class of the wrapper used for initializing the API
 * and retrieving a weather forecast via its several functions
 *
 * @param key
 * An authorization key for the API
 * @param units
 * A measurement unit system chosen by the user
 * @param language
 * A language of the API's responses
 * @param client
 * An optional custom Ktor HTTP client
 */
data class OpenWeatherApi internal constructor(
    val key: String,
    var units: Units = Units.DEFAULT,
    var language: Languages = Languages.ENGLISH,
    val client: HttpClient = HTTP_CLIENT
) {
    companion object {
        const val BASE_API = "http://api.openweathermap.org/data/2.5/weather"

        /**
         * The default Ktor HTTP client for the wrapper
         */
        val HTTP_CLIENT = HttpClient(CIO)
    }

    /**
     * Retrieves the forecast via the name of a location
     *
     * @param name
     * The name of a location
     *
     * @throws IllegalArgumentException
     * If no location is found by the query
     * @throws LoginException
     * If the provided API key is invalid
     *
     * @return A [Forecast] instance based on the API response
     *
     * @see fromNameOrNull
     */
    suspend fun fromName(name: String) =
        getForecast(name)

    /**
     * Retrieves the forecast via the OpenWeatherMap's unique ID of a location
     *
     * @param id
     * The ID of a location
     *
     * @throws IllegalArgumentException
     * If no location is found by the query
     * @throws LoginException
     * If the provided API key is invalid
     *
     * @return A [Forecast] instance based on the API response
     *
     * @see fromIdOrNull
     */
    suspend fun fromId(id: Int) =
        getForecast(id.toString(), FetchMode.ID)

    /**
     * Retrieves the forecast via the zip code of a location
     *
     * @param zip
     * The zip code of a location
     *
     * @throws IllegalArgumentException
     * If no location is found by the query
     * @throws LoginException
     * If the provided API key is invalid
     *
     * @return A [Forecast] instance based on the API response
     *
     * @see fromZipCodeOrNull
     */
    suspend fun fromZipCode(zip: String) =
        getForecast(zip, FetchMode.ZIP_CODE)

    /**
     * Retrieves the forecast via the geographical coordinates of a location
     *
     * @param coords
     * A [Coordinates] instance containing data about the geographical coordinates of a location
     *
     * @throws IllegalArgumentException
     * If no location is found by the query
     * @throws LoginException
     * If the provided API key is invalid
     *
     * @return A [Forecast] instance based on the API response
     *
     * @see fromCoordinatesOrNull
     */
    suspend fun fromCoordinates(coords: Coordinates) =
        getForecast("${coords.longitude}|${coords.latitude}", FetchMode.COORDINATES)

    /**
     * Retrieves the forecast via the name of a location
     *
     * @param name
     * The name of a location
     *
     * @return A [Forecast] instance based on the API response, or null if no location is found
     *
     * @see fromName
     */
    suspend fun fromNameOrNull(name: String) = try {
        fromName(name)
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Retrieves the forecast via the OpenWeatherMap's unique ID of a location
     *
     * @param id
     * The ID of a location
     *
     * @return A [Forecast] instance based on the API response, or null if no location is found
     *
     * @see fromId
     */
    suspend fun fromIdOrNull(id: Int) = try {
        fromId(id)
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Retrieves the forecast via the zip code of a location
     *
     * @param zip
     * The zip code of a location
     *
     * @return A [Forecast] instance based on the API response, or null if no location is found
     *
     * @see fromZipCode
     */
    suspend fun fromZipCodeOrNull(zip: String) = try {
        fromZipCode(zip)
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Retrieves the forecast via the geographical coordinates of a location
     *
     * @param coords
     * A [Coordinates] instance containing data about the geographical coordinates of a location
     *
     * @return A [Forecast] instance based on the API response, or null if no location is found
     *
     * @see fromCoordinates
     */
    suspend fun fromCoordinatesOrNull(coords: Coordinates) = try {
        fromCoordinates(coords)
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * An internal function that retrieves the forecast via the selected type of the query
     * and is utilized for the separate public retrieving functions
     *
     * @see fromName
     * @see fromNameOrNull
     * @see fromId
     * @see fromIdOrNull
     * @see fromZipCode
     * @see fromZipCodeOrNull
     * @see fromCoordinates
     * @see fromCoordinatesOrNull
     */
    private suspend fun getForecast(
        query: String,
        fetchMode: FetchMode = FetchMode.NAME
    ): Forecast {
        val unit = units.raw

        val requestUrl = buildString {
            append(BASE_API)
            append("?appid=$key")
            append("&lang=${language.raw}")

            when (fetchMode) {
                FetchMode.NAME -> append("&q=${URLEncoder.encode(query, "UTF-8")}")
                FetchMode.ID -> append("&id=$query")
                FetchMode.ZIP_CODE -> append("&zip=${URLEncoder.encode(query, "UTF-8")}")
                FetchMode.COORDINATES -> {
                    val coords = query.split("|").take(2)
                        .mapNotNull { it.toFloatOrNull() }

                    if (coords.size != 2)
                        throw IllegalArgumentException("A wrong coordinates format has been used!")

                    append("&lon=${coords[0]}&lat=${coords[1]}")
                }
            }

            unit?.let { append("&units=$it") }
        }

        val response = try {
            client.get<String>(requestUrl)
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Unauthorized ->
                    throw LoginException("UNAUTHORIZED: A wrong API key has been provided!")
                HttpStatusCode.NotFound ->
                    throw IllegalArgumentException("Nothing has been found by the provided query!")
                else ->
                    throw IllegalStateException(e)
            }
        }

        val json = response.toJSONObject()

        val (longitude, latitude) = json.getJSONObject("coord")
            .let { it.getFloat("lon") to it.getFloat("lat") }
        val coords = Coordinates(longitude, latitude)

        val weather = json.getJSONArray("weather").getJSONObject(0)
            .let { Weather(
                it.getInt("id"),
                it.getString("main"),
                it.getString("description"),
                it.getString("icon")
            ) }

        val main = json.getJSONObject("main")

        val temperature = Temperature(
            main.getFloat("temp"),
            main.getFloat("feels_like"),
            main.getFloat("temp_min"),
            main.getFloat("temp_max"),
            Temperature.TemperatureUnit.values().first { it.units == units }
        )
        val pressure = Pressure(
            main.getFloat("pressure"),
            main.getFloatOrNull("sea_level"),
            main.getFloatOrNull("grnd_level")
        )
        val humidity = Humidity(main.getIntOrNull("humidity"))

        val visibility = Visibility(json.getIntOrNull("visibility"))

        val wind = json.getJSONObject("wind")
            .let { Wind(
                it.getFloat("speed"),
                it.getIntOrNull("deg"),
                it.getFloatOrNull("gust"),
                Wind.WindUnit.values().first { u -> units in u.units }
            ) }

        val cloudiness = Cloudiness(json.getJSONObject("clouds").getInt("all"))

        val system = json.getJSONObject("sys")

        val time = system.let { Time(
            json.getInt("timezone"),
            Date(it.getLong("sunrise") * 1000),
            Date(it.getLong("sunset") * 1000)
        ) }

        val location = Location(json.getString("name"), json.getInt("id"), system.getString("country"))

        return Forecast(
            this,
            location,
            cloudiness,
            coords,
            humidity,
            pressure,
            temperature,
            time,
            visibility,
            weather,
            wind
        )
    }

    private enum class FetchMode {
        NAME,
        ID,
        COORDINATES,
        ZIP_CODE
    }
}

class WeatherBuilder {
    lateinit var key: String

    var units = Units.DEFAULT
    var language = Languages.ENGLISH
    var client = OpenWeatherApi.HTTP_CLIENT

    operator fun invoke() = if (::key.isInitialized)
        OpenWeatherApi(key, units, language, client)
    else
        throw LoginException("The provided API key is empty!")
}

/**
 * A function that initializes the API wrapper instance with the provided data
 *
 * @param init
 * A lambda function with a [WeatherBuilder] receiver where the data must be provided
 *
 * @return An [OpenWeatherApi] instance
 */
inline fun openWeatherApi(init: WeatherBuilder.() -> Unit) = WeatherBuilder().apply(init)()

/**
 * A function that initializes the API wrapper instance with the provided data
 *
 * @param key
 * An authorization key for the API
 * @param units
 * A measurement unit system chosen by the user
 * @param language
 * A language of the API's responses
 * @param client
 * An optional custom Ktor HTTP client
 *
 * @return An [OpenWeatherApi] instance
 */
fun openWeatherApi(
    key: String,
    units: Units = Units.DEFAULT,
    language: Languages = Languages.ENGLISH,
    client: HttpClient = OpenWeatherApi.HTTP_CLIENT
) = openWeatherApi {
    this.key = key
    this.units = units
    this.language = language
    this.client = client
}

/**
 * A measurement unit system chosen by the user
 */
enum class Units(val raw: String?) {
    DEFAULT(null),
    METRIC("metric"),
    IMPERIAL("imperial")
}

/**
 * A language of the API's responses
 */
enum class Languages(val raw: String) {
    AFRIKAANS("af"),
    ALBANIAN("al"),
    ARABIC("ar"),
    AZERBAIJANI("az"),
    BASQUE("eu"),
    BULGARIAN("bg"),
    CATALAN("ca"),
    CHINESE_SIMPLIFIED("zh_cn"),
    CHINESE_TRADITIONAL("zh_tw"),
    CROATIAN("hr"),
    CZECH("cz"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    FARSI("fa"),
    FINNISH("fi"),
    FRENCH("fr"),
    GALICIAN("gl"),
    GERMAN("de"),
    GREEK("el"),
    HEBREW("he"),
    HINDI("hi"),
    HUNGARIAN("hu"),
    INDONESIAN("id"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("kr"),
    LATVIAN("la"),
    LITHUANIAN("lt"),
    MACEDONIAN("mk"),
    NORWEGIAN("no"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PORTUGUESE_BRAZIL("pt_br"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SERBIAN("sr"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("se"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("ua"),
    VIETNAMESE("vi"),
    ZULU("zu")
}