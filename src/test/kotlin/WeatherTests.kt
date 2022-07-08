import io.ileukocyte.openweather.Units
import io.ileukocyte.openweather.entities.Coordinates
import io.ileukocyte.openweather.openWeatherApi

import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import java.lang.IllegalArgumentException
import javax.security.auth.login.LoginException

internal class WeatherTests {
    @Test
    fun `weather api main tests`() {
        runBlocking {
            val api = openWeatherApi(System.getenv("WEATHER_API_KEY"), Units.METRIC)

            println(api.fromName("kyiv"))
            println(api.fromName("москва"))

            println(api.fromId(2643743))
            println(api.fromCoordinates(Coordinates(-0.1257f, 51.5085f)))

            println(api.fromZipCode("94040,us"))

            assert(api.fromNameOrNull("Washington D.C.") !== null)
            assert(api.fromNameOrNull("nothing must be found") === null)

            assertThrows<IllegalArgumentException> { runBlocking { println(api.fromName("nothing must be found")) } }
            assertThrows<LoginException> {
                runBlocking { println(openWeatherApi("none").fromName("nothing must be found")) }
            }
            assertThrows<LoginException> {
                runBlocking { println(openWeatherApi {}) }
            }
        }
    }
}