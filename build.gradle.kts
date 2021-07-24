import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val coroutinesVersion: String by project

plugins {
    java
    application

    kotlin("jvm") version "1.5.21"

    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.ben-manes.versions") version "0.19.0"
}

group = "io.ileukocyte"
version = Version(major = 1, minor = 0, unstable = 1, stability = Version.Stability.Beta)

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "io.ktor", name = "ktor-client-core", version = ktorVersion)
    implementation(group = "io.ktor", name = "ktor-client-cio", version = ktorVersion)

    implementation(group = "org.json", name = "json", version = "20210307")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlinx("coroutines-core", version = coroutinesVersion))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"

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
    val unstable: Int = 0
) {
    override fun toString() = arrayOf(
        major,
        minor,
        patch.takeUnless { it == 0 }
    ).filterNotNull().joinToString(separator = ".") + stability.suffix?.let { "-$it$unstable" }.orEmpty()

    sealed class Stability(val suffix: String? = null) {
        object Stable : Stability()
        object ReleaseCandidate : Stability("RC")
        object Beta : Stability("BETA")
        object Alpha : Stability("ALPHA")

        override fun toString() = suffix ?: "STABLE"
    }
}
