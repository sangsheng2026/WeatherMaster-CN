package com.pranshulgg.weather_master_app.core.network.sources.weather.china.json

import com.google.gson.annotations.SerializedName

data class ChinaForecastJson(
    val current: ChinaCurrentForecastJson,
    val forecastHourly: ChinaHourlyForecastJson,
    val forecastDaily: ChinaDailyForecastJson,

    /** Official AQI, published by 中国环境监测总站 (CNEMC). */
    val aqi: ChinaAqiJson? = null,

    /** Official weather alerts published by the local meteorological bureau. */
    val alerts: List<ChinaAlertJson>? = null,
)

data class ChinaCurrentForecastJson(
    val feelsLike: ChinaCurrentForecastFeelsLikeJson,
    val humidity: ChinaCurrentForecastHumidityJson,
    val pressure: ChinaCurrentForecastPressureJson,
    val pubTime: String,
    val temperature: ChinaCurrentForecastTemperatureJson,
    val uvIndex: String?,
    val visibility: ChinaCurrentForecastVisibilityJson,
    val weather: String?,
    val wind: ChinaCurrentForecastWindJson
)

data class ChinaCurrentForecastFeelsLikeJson(
    val value: String?
)

data class ChinaCurrentForecastHumidityJson(
    val value: String?
)

data class ChinaCurrentForecastPressureJson(
    val value: String?
)

data class ChinaCurrentForecastTemperatureJson(
    val value: String?
)

data class ChinaCurrentForecastVisibilityJson(
    val value: String?
)

data class ChinaCurrentForecastWindJson(
    val speed: ChinaCurrentForecastWindSpeedJson,
    val direction: ChinaCurrentForecastWindDirectionJson
)

data class ChinaCurrentForecastWindSpeedJson(
    val value: String?
)

data class ChinaCurrentForecastWindDirectionJson(
    val value: String?
)

// HOURLY
data class ChinaHourlyForecastJson(
    val temperature: ChinaHourlyForecastTemperatureJson,
    val weather: ChinaHourlyForecastWeatherJson,
    val wind: ChinaHourlyForecastWindJson,

    /** Per-hour AQI (China Weather / 彩云), one value per hour. */
    val aqi: ChinaForecastValueListJson? = null,
)

data class ChinaHourlyForecastTemperatureJson(
    val value: List<Int>
)


data class ChinaHourlyForecastWeatherJson(
    val value: List<Int>
)


data class ChinaHourlyForecastWindJson(
    val value: List<ChinaHourlyForecastWindValueJson>
)

data class ChinaHourlyForecastWindValueJson(
    val datetime: String,
    val direction: String?,
    val speed: String?
)

/** Generic `{"value": [..]}` wrapper used by a few Xiaomi fields. */
data class ChinaForecastValueListJson(
    val value: List<Int>? = null,
)

// DAILY
data class ChinaDailyForecastJson(
    val precipitationProbability: ChinaDailyForecastPrecipitationProbabilityJson,
    val pubTime: String,
    val temperature: ChinaDailyForecastTemperatureJson,
    val weather: ChinaDailyForecastWeatherJson,
    val wind: ChinaDailyForecastWindJson
)

data class ChinaDailyForecastPrecipitationProbabilityJson(
    val value: List<String?>
)

data class ChinaDailyForecastTemperatureJson(
    val value: List<ChinaDailyForecastTemperatureValueJson>
)

data class ChinaDailyForecastTemperatureValueJson(
    @SerializedName("from") val max: String?,
    @SerializedName("to") val min: String?,
)

data class ChinaDailyForecastWeatherJson(
    val value: List<ChinaDailyForecastWeatherValueJson>
)

data class ChinaDailyForecastWeatherValueJson(
    val from: String?,
    val to: String?,
)

data class ChinaDailyForecastWindJson(
    val direction: ChinaDailyForecastWindDirectionJson,
    val speed: ChinaDailyForecastWindSpeedJson
)

data class ChinaDailyForecastWindDirectionJson(
    val value: List<ChinaDailyForecastWindDirectionValueJson>
)

data class ChinaDailyForecastWindDirectionValueJson(
    val from: String?,
    val to: String?,
)

data class ChinaDailyForecastWindSpeedJson(
    val value: List<ChinaDailyForecastWindSpeedValueJson>
)

data class ChinaDailyForecastWindSpeedValueJson(
    val from: String?,
    val to: String?,
)

// AIR QUALITY
data class ChinaAqiJson(
    /** Index on the Chinese national scale (0–500). */
    val aqi: String? = null,
    val pm10: String? = null,
    val pm25: String? = null,
    /** Milligrams per cubic meter (unlike Open-Meteo, which uses micrograms). */
    val co: String? = null,
    val no2: String? = null,
    val o3: String? = null,
    val so2: String? = null,
    val primary: String? = null,
    /** e.g. "中国环境监测总站" */
    val src: String? = null,
    val pubTime: String? = null,
    val suggest: String? = null,
)

// ALERTS
data class ChinaAlertJson(
    val alertId: String? = null,
    val title: String? = null,
    /** e.g. 大风 */
    val type: String? = null,
    /** e.g. 蓝色 / 黄色 / 橙色 / 红色 */
    val level: String? = null,
    val detail: String? = null,
    val pubTime: String? = null,
    val defense: List<ChinaAlertDefenseJson>? = null,
)

data class ChinaAlertDefenseJson(
    val defenseText: String? = null,
)