package io.ileukocyte.openweather.extensions.internal

import org.json.JSONObject

internal fun String.toJSONObject() = JSONObject(this)

internal fun JSONObject.getIntOrNull(key: String) = if (has(key)) getInt(key) else null
internal fun JSONObject.getFloatOrNull(key: String) = if (has(key)) getFloat(key) else null