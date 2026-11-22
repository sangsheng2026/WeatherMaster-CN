package com.pranshulgg.weather_master_app.core.network.sources.weather.china

import com.google.gson.annotations.SerializedName
import com.pranshulgg.weather_master_app.core.model.domain.weather.Weather
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * The Xiaomi/China Weather API only exposes current humidity, pressure and a few
 * other fields. Per-hour series for humidity, pressure, dew point, UV and
 * visibility are missing, which makes every detail screen fall back to
 * "no data" (those screens require a complete hourly column).
 *
 * This is a small, deliberately lightweight Open-Meteo request that only asks
 * for the missing hourly fields so the China source can fill those gaps.
 * It is best-effort: callers must treat a failure as "no supplement".
 */
interface ChinaHourlySupplementApi {

    @GET("forecast")
    suspend fun fetch(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("timeformat") timeFormat: String = "unixtime",
    ): Response<ChinaSupplementJson>

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/v1/"

        private const val HOURLY_FIELDS =
            "relative_humidity_2m,dew_point_2m,pressure_msl,uv_index,visibility,precipitation_probability"

        private const val CURRENT_FIELDS =
            "relative_humidity_2m,dew_point_2m,pressure_msl,uv_index,visibility"

        fun create(): ChinaHourlySupplementApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChinaHourlySupplementApi::class.java)
        }
    }
}

data class ChinaSupplementJson(
    val hourly: Hourly?,
    val current: Current?,
) {

    data class Hourly(
        val time: List<Long>?,
        @SerializedName("relative_humidity_2m") val humidity: List<Double?>?,
        @SerializedName("dew_point_2m") val dewPoint: List<Double?>?,
        @SerializedName("pressure_msl") val pressureMsl: List<Double?>?,
        @SerializedName("uv_index") val uvIndex: List<Double?>?,
        val visibility: List<Double?>?,
        @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>?,
    )

    data class Current(
        @SerializedName("relative_humidity_2m") val humidity: Double?,
        @SerializedName("dew_point_2m") val dewPoint: Double?,
        @SerializedName("pressure_msl") val pressureMsl: Double?,
        @SerializedName("uv_index") val uvIndex: Double?,
        val visibility: Double?,
    )
}

private const val HOUR_MS = 60L * 60L * 1000L

private fun hourStart(milli: Long): Long = Math.floorDiv(milli, HOUR_MS) * HOUR_MS

private data class HourlyPatch(
    val humidity: Double?,
    val dewPoint: Double?,
    val pressureMsl: Double?,
    val uvIndex: Double?,
    val visibility: Int?,
    val precipitationProbability: Int?,
)

/**
 * Fills the fields the China source cannot provide, without overwriting any
 * value the China response already supplied.
 */
fun Weather.withOpenMeteoSupplement(supplement: ChinaSupplementJson?): Weather {
    if (supplement == null) return this

    val patches = buildHourlyPatches(supplement.hourly)

    val mergedHourly = hourly.map { item ->
        val patch = patches[hourStart(item.time)] ?: return@map item
        item.copy(
            humidity = item.humidity ?: patch.humidity,
            dewPoint = item.dewPoint ?: patch.dewPoint,
            pressureMsl = item.pressureMsl ?: patch.pressureMsl,
            uvIndex = item.uvIndex ?: patch.uvIndex,
            visibility = item.visibility ?: patch.visibility,
            precipitationProbability = item.precipitationProbability
                ?: patch.precipitationProbability,
        )
    }

    val supplementCurrent = supplement.current
    val current = if (supplementCurrent == null) {
        this.current
    } else {
        this.current.copy(
            humidity = this.current.humidity ?: supplementCurrent.humidity,
            dewPoint = this.current.dewPoint ?: supplementCurrent.dewPoint,
            pressureMsl = this.current.pressureMsl ?: supplementCurrent.pressureMsl,
            uvIndex = this.current.uvIndex ?: supplementCurrent.uvIndex,
            visibility = this.current.visibility ?: supplementCurrent.visibility?.toInt(),
        )
    }

    return copy(current = current, hourly = mergedHourly)
}

private fun buildHourlyPatches(
    hourly: ChinaSupplementJson.Hourly?,
): Map<Long, HourlyPatch> {
    val times = hourly?.time ?: return emptyMap()

    return times.withIndex().associate { (index, seconds) ->
        hourStart(seconds * 1000L) to HourlyPatch(
            humidity = hourly.humidity?.getOrNull(index),
            dewPoint = hourly.dewPoint?.getOrNull(index),
            pressureMsl = hourly.pressureMsl?.getOrNull(index),
            uvIndex = hourly.uvIndex?.getOrNull(index),
            visibility = hourly.visibility?.getOrNull(index)?.toInt(),
            precipitationProbability = hourly.precipitationProbability?.getOrNull(index),
        )
    }
}