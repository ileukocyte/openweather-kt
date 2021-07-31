package io.ileukocyte.openweather

object OpenWeatherKtInfo {
    val VERSION = Version(major = 1, minor = 0)

    const val GITHUB = "https://github.com/ileukocyte/openweather-kt"

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
}