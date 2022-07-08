@file:[JvmName("OpenWeatherKt") Suppress("UNUSED")]
package io.ileukocyte.openweather

import io.ileukocyte.openweather.entities.*

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.serialization.kotlinx.json.json

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.Date
import javax.security.auth.login.LoginException

import kotlinx.serialization.json.*

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
    val client: HttpClient = HTTP_CLIENT,
) {
    companion object {
        const val BASE_API = "http://api.openweathermap.org/data/2.5/weather"

        /**
         * The default Ktor HTTP client for the wrapper
         */
        val HTTP_CLIENT = HttpClient(CIO) {
            install(ContentNegotiation) { json() }
        }
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

        val queryParam = when (fetchMode) {
            FetchMode.NAME -> setOf("q" to query)
            FetchMode.ID -> setOf("id" to query)
            FetchMode.ZIP_CODE -> setOf("zip" to query)
            FetchMode.COORDINATES -> {
                val coords = query.split("|").take(2)
                    .mapNotNull { it.toFloatOrNull() }

                if (coords.size != 2)
                    throw IllegalArgumentException("A wrong coordinates format has been used!")

                setOf("lon" to "${coords[0]}", "lat" to "${coords[1]}")
            }
        }

        val params = mutableMapOf(
            "appid" to key,
            "lang" to language.raw,
            unit?.let { "units" } to unit.orEmpty()
        )

        queryParam.forEach { (k, v) -> params[k] = v }

        val response = client
            .get("$BASE_API?${params.filter { it.key !== null }.map { it.key!! to it.value }.formUrlEncode()}")
            .apply {
                when (status) {
                    HttpStatusCode.OK -> {}
                    HttpStatusCode.Unauthorized ->
                        throw LoginException("UNAUTHORIZED: A wrong API key has been provided!")
                    HttpStatusCode.NotFound ->
                        throw IllegalArgumentException("Nothing has been found by the provided query!")
                    else ->
                        throw IllegalStateException()
                }
            }.body<JsonObject>()

        val (longitude, latitude) = response["coord"]!!.jsonObject
            .let { it["lon"]!!.jsonPrimitive.float to it["lat"]!!.jsonPrimitive.float }
        val coords = Coordinates(longitude, latitude)

        val weather = response["weather"]!!.jsonArray.first().jsonObject
            .let { Weather(
                it["id"]!!.jsonPrimitive.int,
                it["main"]!!.jsonPrimitive.toString(),
                it["description"]!!.jsonPrimitive.toString(),
                it["icon"]!!.jsonPrimitive.toString(),
            ) }

        val main = response["main"]!!.jsonObject

        val temperature = Temperature(
            main["temp"]!!.jsonPrimitive.float,
            main["feels_like"]!!.jsonPrimitive.float,
            main["temp_min"]!!.jsonPrimitive.float,
            main["temp_max"]!!.jsonPrimitive.float,
            Temperature.TemperatureUnit.values().first { it.units == units },
        )

        val pressure = Pressure(
            main["pressure"]!!.jsonPrimitive.float,
            main["sea_level"]?.jsonPrimitive?.floatOrNull,
            main["grnd_level"]?.jsonPrimitive?.floatOrNull,
        )

        val humidity = Humidity(main["humidity"]?.jsonPrimitive?.intOrNull)

        val visibility = Visibility(response["visibility"]?.jsonPrimitive?.intOrNull)

        val wind = response["wind"]!!.jsonObject
            .let { Wind(
                it["speed"]!!.jsonPrimitive.float,
                it["deg"]?.jsonPrimitive?.intOrNull,
                it["gust"]?.jsonPrimitive?.floatOrNull,
                Wind.WindUnit.values().first { u -> units in u.units },
            ) }

        val cloudiness = Cloudiness(response["clouds"]!!.jsonObject["all"]!!.jsonPrimitive.int)

        val system = response["sys"]!!.jsonObject

        val time = system.let { Time(
            response["timezone"]!!.jsonPrimitive.int,
            Date(it["sunrise"]!!.jsonPrimitive.long * 1000),
            Date(it["sunset"]!!.jsonPrimitive.long * 1000),
        ) }

        val location = Location(
            response["name"]!!.jsonPrimitive.toString(),
            response["id"]!!.jsonPrimitive.int,
            system["country"]!!.jsonPrimitive.toString(),
        )

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
            wind,
        )
    }

    private enum class FetchMode {
        NAME,
        ID,
        COORDINATES,
        ZIP_CODE,
    }
}

class WeatherBuilder {
    lateinit var key: String

    var units = Units.DEFAULT
    var language = Languages.ENGLISH
    var client = OpenWeatherApi.HTTP_CLIENT

    operator fun invoke() = if (::key.isInitialized) {
        OpenWeatherApi(key, units, language, client)
    } else throw LoginException("The provided API key is empty!")
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
    client: HttpClient = OpenWeatherApi.HTTP_CLIENT,
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
    IMPERIAL("imperial"),
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
    ZULU("zu"),
}