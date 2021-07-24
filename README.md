# openweather-kt [![JitPack](https://jitpack.io/v/iileukocyte/openweather-kt.svg)](https://jitpack.io/#ileukocyte/openweather-kt)
openweather-kt is a simple Kotlin wrapper for Current Weather API by OpenWeatherMap.

## Examples
### #1:
```kotlin
val api = openWeatherApi {
    key = OWM_API_KEY
    units = Units.IMPERIAL
}

val forecast = api.fromName("cupertino") // throws IllegalArgumentException if nothing has been found

println(forecast.wind.directionName)
```
### #2:
```kotlin
val api = openWeatherApi(OWM_API_KEY, language = Languages.RUSSIAN)

val forecast = api.fromNameOrNull("London")
val temperature = forecast?.temperature?.convertTo(TemperatureUnit.CELCIUS_DEGREE)

println("${temperature?.temperature ?: 0}${temperature?.unit?.symbol ?: "K"}")
```
## Usage
Replace **VERSION** keyword with the latest commit or the latest release in the `master` branch.
#### Gradle (Kotlin):
```kotlin
repositories {
    jcenter()

    mavenCentral()

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(group = "com.github.ileukocyte", name = "openweather-kt", version = "VERSION")
}
```
#### Gradle:
```groovy
repositories {
    jcenter()

    mavenCentral()

    maven {
        name 'jitpack'
        url 'https://jitpack.io'
    }
}

dependencies {
    compile "com.github.ileukocyte:openweather-kt:VERSION"
}
```
#### Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.ileukocyte</groupId>
        <artifactId>openweather-kt</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```