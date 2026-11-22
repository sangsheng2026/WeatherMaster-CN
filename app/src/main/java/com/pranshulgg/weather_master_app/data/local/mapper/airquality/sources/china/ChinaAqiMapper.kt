package com.pranshulgg.weather_master_app.data.local.mapper.airquality.sources.china

import com.pranshulgg.weather_master_app.core.model.domain.airquality.AirQuality
import com.pranshulgg.weather_master_app.core.model.domain.airquality.AirQualityCurrent
import com.pranshulgg.weather_master_app.core.model.domain.airquality.AirQualityHourly
import com.pranshulgg.weather_master_app.core.network.sources.weather.china.json.ChinaForecastJson
import com.pranshulgg.weather_master_app.core.utils.extensions.DateTimeExtensions.iso8601TimestampToMilliseconds

/**
 * Maps the Xiaomi / China Weather air-quality payload.
 *
 * The current block carries the official CNEMC index together with pollutant
 * concentrations. The hourly block only carries the index — no per-hour
 * pollutant values — so those are left null and the screens fall back to
 * [AirQuality.getAqiForHourly], which prefers the published index.
 */
fun ChinaForecastJson.toAirQuality(): AirQuality? {
    val aqiJson = this.aqi ?: return null

    val reportedAqi = aqiJson.aqi?.toDoubleOrNull()?.toInt()?.takeIf { it > 0 }

    val current = AirQualityCurrent(
        usAqi = reportedAqi,
        pm10 = aqiJson.pm10?.toDoubleOrNull(),
        pm25 = aqiJson.pm25?.toDoubleOrNull(),
        // Xiaomi reports CO in mg/m³; the domain model stores µg/m³.
        carbonMonoxide = aqiJson.co?.toDoubleOrNull()?.times(1000),
        nitrogenDioxide = aqiJson.no2?.toDoubleOrNull(),
        sulphurDioxide = aqiJson.so2?.toDoubleOrNull(),
        ozone = aqiJson.o3?.toDoubleOrNull(),
        lastUpdatedInMilli = aqiJson.pubTime?.iso8601TimestampToMilliseconds()
            ?: System.currentTimeMillis(),
        reportedAqi = reportedAqi
    )

    if (current.pm25 == null && current.pm10 == null && reportedAqi == null) {
        return null
    }

    return AirQuality(current = current, hourly = buildHourly(this))
}

private fun buildHourly(json: ChinaForecastJson): List<AirQualityHourly> {
    val values = json.forecastHourly.aqi?.value ?: return emptyList()
    val times = json.forecastHourly.wind.value

    return values.mapIndexedNotNull { index, value ->
        val timeMilli = times.getOrNull(index)?.datetime?.iso8601TimestampToMilliseconds()
            ?: return@mapIndexedNotNull null

        AirQualityHourly(
            pm10 = null,
            pm25 = null,
            carbonMonoxide = null,
            nitrogenDioxide = null,
            sulphurDioxide = null,
            ozone = null,
            // The domain model stores seconds here (screens convert to millis).
            time = timeMilli / 1000,
            aqi = value.takeIf { it > 0 }
        )
    }
}