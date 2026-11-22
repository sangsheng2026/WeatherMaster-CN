package com.pranshulgg.weather_master_app.core.model.domain.airquality

import com.pranshulgg.weather_master_app.core.model.weather.airquality.AirQualityLevel
import kotlin.math.roundToInt

// TODO: ADD HOURLY-DAILY FORECAST

/**
 * Keeping air quality separate from the main weather domain
 * We don't wanna delay weather from displaying because the API hasn't returned anything or is slow
 * We only display air quality if it's available
 */
data class AirQuality(
    val current: AirQualityCurrent,
    val hourly: List<AirQualityHourly>
) {
    companion object {

        /**
         * Thresholds extracted from Breezy Weather
         * [https://github.com/breezy-weather/breezy-weather/blob/06c4f1bbd6f27f8ae04ebba550cf8981e362e777/app/src/main/kotlin/org/breezyweather/domain/weather/index/PollutantIndex.kt]
         */
        private val pm25Thresholds = listOf(0, 5, 15, 30, 60, 150)
        private val pm10Thresholds = listOf(0, 15, 45, 80, 160, 400)
        private val o3Thresholds = listOf(0, 50, 100, 160, 240, 480)

        private val so2Thresholds = listOf(0, 20, 40, 270, 500, 960)
        private val no2Thresholds = listOf(0, 10, 25, 200, 400, 1000)

        private val coThresholds = listOf(0, 2, 4, 35, 100, 230)

        private val aqiThreshold = listOf(0, 20, 50, 100, 150, 250)

        /**
         * Chinese national scale (HJ 633-2012) used for the level badges.
         * The source reports its index on this scale, so levels must be judged
         * against it or a "31 优" reading would be shown as "良".
         */
        private val displayAqiThreshold = listOf(0, 50, 100, 150, 200, 300)

        private fun getAqi(value: Double, thresholds: List<Int>): Int {
            val index = thresholds.indexOfLast { value >= it }

            if (index == -1) return 0
            if (index >= thresholds.lastIndex) return aqiThreshold.last()


            /**
             * Get threshold range
             * For example: thresholds [0, 5, 15, 30, 60, 150]
             * If value is 21, the index should return 2 because 21 is more than 15 but less than 30
             * So we get the threshold range 15 - 30
             */
            val threshold1 = thresholds[index]
            val threshold2 = thresholds[index + 1]

            /**
             * Do same for AQI
             * For example: aqi thresholds [0, 20, 50, 100, 150, 250]
             * If threshold range 15 - 30, we should get 50 - 100
             */
            val aqi1 = aqiThreshold[index]
            val aqi2 = aqiThreshold[index + 1]

            val pos = (value - threshold1) / (threshold2 - threshold1)

            return (aqi1 + pos * (aqi2 - aqi1)).roundToInt()

        }
    }


    fun getAqi(): Int {

        // Sources that report a ready-made index (e.g. China / CNEMC) take priority
        current.reportedAqi?.takeIf { it > 0 }?.let { return it }

        // Open-Meteo provides "carbon_monoxide" in MicrogramsPerCubicMeter so we convert to Milligrams
        return getAqiFromValues(
            carbonMonoxideMicrogramsPerCubicMeter = current.carbonMonoxide,
            pm25 = current.pm25,
            nitrogenDioxide = current.nitrogenDioxide,
            ozone = current.ozone,
            pm10 = current.pm10,
            sulphurDioxide = current.sulphurDioxide
        )
    }

    /**
     * Any missing pollutant is simply skipped instead of crashing: not every
     * source supplies every pollutant (and per-hour series may only carry the
     * already-computed index).
     */
    fun getAqiFromValues(
        carbonMonoxideMicrogramsPerCubicMeter: Double?,
        pm25: Double?,
        nitrogenDioxide: Double?,
        ozone: Double?,
        pm10: Double?,
        sulphurDioxide: Double?
    ): Int {

        // Open-Meteo provides "carbon_monoxide" in MicrogramsPerCubicMeter so we convert to Milligrams
        val coInMilligramsPerCubicMeter = carbonMonoxideMicrogramsPerCubicMeter?.div(1000)

        return maxOf(
            aqiOrZero(pm25, pm25Thresholds),
            aqiOrZero(nitrogenDioxide, no2Thresholds),
            aqiOrZero(ozone, o3Thresholds),
            aqiOrZero(pm10, pm10Thresholds),
            aqiOrZero(coInMilligramsPerCubicMeter, coThresholds),
            aqiOrZero(sulphurDioxide, so2Thresholds)
        )
    }

    /**
     * AQI for one hourly entry: use the reported index when the source provides
     * one, otherwise derive it from whatever pollutant values exist.
     */
    fun getAqiForHourly(hourly: AirQualityHourly): Int =
        hourly.aqi?.takeIf { it > 0 } ?: getAqiFromValues(
            carbonMonoxideMicrogramsPerCubicMeter = hourly.carbonMonoxide,
            pm25 = hourly.pm25,
            nitrogenDioxide = hourly.nitrogenDioxide,
            ozone = hourly.ozone,
            pm10 = hourly.pm10,
            sulphurDioxide = hourly.sulphurDioxide
        )

    private fun aqiOrZero(value: Double?, thresholds: List<Int>): Int =
        value?.let { getAqi(it, thresholds) } ?: 0

    // For AQI bars
    fun getAqiBarValue(aqi: Int): Float {
        return (aqi / 300f).coerceIn(0f, 1f)
    }

    private fun getAqiIndex(value: Int): Int {
        val index = displayAqiThreshold.indexOfLast { value >= it }
        return if (index == -1) 0 else index
    }

    fun getAqiLevel(aqi: Int): AirQualityLevel {

        val level = getAqiIndex(aqi)

        return when (level) {
            0 -> AirQualityLevel.GOOD
            1 -> AirQualityLevel.FAIR
            2 -> AirQualityLevel.MODERATE
            3 -> AirQualityLevel.POOR
            4 -> AirQualityLevel.VERY_POOR
            else -> AirQualityLevel.HAZARDOUS
        }
    }
}

data class AirQualityCurrent(
    val usAqi: Int?,

    /** µg/m³. */
    val pm10: Double?,

    /** µg/m³. */
    val pm25: Double?,

    /** µg/m³. */
    val carbonMonoxide: Double?,

    /** µg/m³. */
    val nitrogenDioxide: Double?,

    /** µg/m³. */
    val sulphurDioxide: Double?,

    /** µg/m³. */
    val ozone: Double?,

    /** Milliseconds */
    val lastUpdatedInMilli: Long,

    /**
     * Index as published by the source itself (China / CNEMC), used as-is when
     * present. Null for sources where the app derives the index locally.
     */
    val reportedAqi: Int? = null
)

data class AirQualityHourly(
    /** µg/m³. */
    val pm10: Double?,

    /** µg/m³. */
    val pm25: Double?,

    /** µg/m³. */
    val carbonMonoxide: Double?,

    /** µg/m³. */
    val nitrogenDioxide: Double?,

    /** µg/m³. */
    val sulphurDioxide: Double?,

    /** µg/m³. */
    val ozone: Double?,

    /** Seconds */
    val time: Long,

    /**
     * Index as published by the source for this hour (China / China Weather).
     * Null when the source only exposes pollutant concentrations.
     */
    val aqi: Int? = null,
)
