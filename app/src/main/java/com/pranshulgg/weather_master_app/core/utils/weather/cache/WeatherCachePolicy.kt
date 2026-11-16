package com.pranshulgg.weather_master_app.core.utils.weather.cache

import android.util.Log
import com.pranshulgg.weather_master_app.core.model.domain.weather.Weather
import com.pranshulgg.weather_master_app.core.model.weather.WeatherResultType
import com.pranshulgg.weather_master_app.core.utils.weather.cache.CacheConfig.AUTO_REFRESH_MAX_MINUTES
import com.pranshulgg.weather_master_app.core.utils.weather.cache.CacheConfig.MANUAL_REFRESH_MINUTES
import com.pranshulgg.weather_master_app.data.local.entity.weather.WeatherWithRelations
import java.util.concurrent.TimeUnit

fun isWeatherCacheSafe(cache: WeatherWithRelations?): Boolean {


    val isSafe = cache != null &&
            cache.daily.isNotEmpty() &&
            cache.hourly.isNotEmpty() &&
            cache.current != null

    return isSafe
}

fun isWeatherDomainSafe(weather: Weather?): Boolean {
    val isSafe = weather != null &&
            weather.daily.isNotEmpty() &&
            weather.hourly.isNotEmpty()
    return isSafe
}

fun isWeatherHourlyDomainSafe(weather: Weather?): Boolean {
    val isSafe =
        weather != null &&
                weather.hourly.isNotEmpty()

    return isSafe
}

fun isWeatherDailyDomainSafe(weather: Weather?): Boolean {
    val isSafe =
        weather != null &&
                weather.daily.isNotEmpty()

    return isSafe
}

fun shouldReturnWeatherCache(
    cache: WeatherWithRelations?,
    isManualRefresh: Boolean,
    isForceRefresh: Boolean
): WeatherResultType {

    if (isForceRefresh) return WeatherResultType.ERROR

    if (!isWeatherCacheSafe(cache)) return WeatherResultType.ERROR

    val cacheMilli = cache!!.current!!.lastUpdatedInMilli
    val ageMillis = System.currentTimeMillis() - cacheMilli
    val ageMinutes = TimeUnit.MILLISECONDS.toMinutes(ageMillis)

    // Manual refresh always goes to the network: this build intentionally has no
    // "please retry in N minutes" cooldown (the fixed 15-minute notice that used to
    // show up on pull-to-refresh).
    if (isManualRefresh) return WeatherResultType.ERROR

    val shouldReturnCache = ageMinutes < AUTO_REFRESH_MAX_MINUTES

    return if (shouldReturnCache) WeatherResultType.SUCCESS else WeatherResultType.ERROR
}