import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project

plugins {
    java
    application

    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"

    /*id("maven")
    id("maven-publish")*/

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.ben-manes.versions") version "0.42.0"
}

group = "io.ileukocyte"
version = Version(major = 1, minor = 1)

repositories {
    mavenCentral()
}

dependencies {
    implementation(ktor("client-content-negotiation"))
    implementation(ktor("client-core"))
    implementation(ktor("client-cio"))
    implementation(ktor("serialization-kotlinx-json"))

    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlinx("coroutines-core", version = "1.6.3"))
    implementation(kotlinx("serialization-json", "1.3.3"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"

fun ktor(module: String, version: String = ktorVersion) = "io.ktor:ktor-$module:$version"

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

val build: DefaultTask by tasks
val clean: Delete by tasks
val jar: Jar by tasks
val shadowJar: ShadowJar by tasks

build.apply {
    dependsOn(clean)
    dependsOn(shadowJar)

    jar.mustRunAfter(clean)
}

tasks.withType<ShadowJar> {
    project.setProperty("mainClassName", "io.ileukocyte.openweather.OpenWeatherApi")

    archiveBaseName.set("openweather-kt")
    archiveClassifier.set("")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int = 0,
    val stability: Stability = Stability.Stable,
    val unstable: Int = 0,
) {
    override fun toString() = arrayOf(
        major,
        minor,
        patch.takeUnless { it == 0 },
    ).filterNotNull().joinToString(separator = ".") +
            stability.suffix?.let { "-$it$unstable" }.orEmpty()

    sealed class Stability(val suffix: String? = null) {
        object Stable : Stability()
        object ReleaseCandidate : Stability("RC")
        object Beta : Stability("BETA")
        object Alpha : Stability("ALPHA")

        override fun toString() = suffix ?: "STABLE"
    }
}
