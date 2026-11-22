package com.pranshulgg.weather_master_app.core.network.sources.weather.china

import com.pranshulgg.weather_master_app.core.model.domain.AppException
import com.pranshulgg.weather_master_app.core.model.domain.airquality.AirQuality
import com.pranshulgg.weather_master_app.core.model.domain.alerts.Alert
import com.pranshulgg.weather_master_app.core.model.domain.location.Location
import com.pranshulgg.weather_master_app.core.model.domain.weather.Weather
import com.pranshulgg.weather_master_app.core.model.sources.Source
import com.pranshulgg.weather_master_app.core.model.weather.FinishedWeatherResult
import com.pranshulgg.weather_master_app.core.model.weather.WeatherDataPack
import com.pranshulgg.weather_master_app.core.model.weather.airquality.AirQualityDataPack
import com.pranshulgg.weather_master_app.core.model.weather.airquality.FinishedAirQualityResult
import com.pranshulgg.weather_master_app.core.model.weather.alerts.AlertsDataPack
import com.pranshulgg.weather_master_app.core.model.weather.alerts.FinishedAlertsResult
import com.pranshulgg.weather_master_app.core.network.calls.safeApiCall
import com.pranshulgg.weather_master_app.core.network.sources.weather.china.json.ChinaForecastJson
import com.pranshulgg.weather_master_app.data.local.dao.airquality.AirQualityDao
import com.pranshulgg.weather_master_app.data.local.dao.alerts.AlertsDao
import com.pranshulgg.weather_master_app.data.local.dao.weather.WeatherContextDao
import com.pranshulgg.weather_master_app.data.local.dao.weather.WeatherDao
import com.pranshulgg.weather_master_app.data.local.mapper.airquality.sources.china.toAirQuality
import com.pranshulgg.weather_master_app.data.local.mapper.alerts.sources.china.toDomain
import com.pranshulgg.weather_master_app.data.local.mapper.weather.sources.china.toDomain
import com.pranshulgg.weather_master_app.data.repository.airquality.AirQualityCacheModel
import com.pranshulgg.weather_master_app.data.repository.alerts.AlertCacheModel
import com.pranshulgg.weather_master_app.data.repository.capability.AirQualityCapability
import com.pranshulgg.weather_master_app.data.repository.capability.AlertCapability
import com.pranshulgg.weather_master_app.data.repository.capability.WeatherCapability
import com.pranshulgg.weather_master_app.data.repository.data.BaseRepository
import com.pranshulgg.weather_master_app.data.repository.weather.CacheModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ChinaRepository @Inject constructor(
    val dao: WeatherContextDao,
    val weatherDao: WeatherDao,
    val airQualityDao: AirQualityDao,
    val alertsDao: AlertsDao,
    val api: ChinaApi,
    val supplementApi: ChinaHourlySupplementApi
) : BaseRepository() {

    override val weatherSource = Source.CHINA
    override val alertSource = Source.CHINA
    override val airQualitySource = Source.CHINA

    // Air quality and alerts arrive inside the same weather payload
    override val providesAlerts = true
    override val providesAirQuality = true

    /**
     * The weather response also carries the air-quality and alert blocks, so it
     * is kept here for the capability calls that follow (the caller waits for
     * the weather job before asking for air quality / alerts).
     */
    @Volatile
    private var lastForecast: ChinaForecastJson? = null

    override fun weatherCapability(): WeatherCapability? {
        return object : WeatherCapability {
            override suspend fun fetchAndProcess(
                location: Location,
                isManualRefresh: Boolean,
                isForceRefresh: Boolean,
                cacheModel: CacheModel
            ): WeatherDataPack = coroutineScope {
                val appKey = "weather20151024"
                val sign = "zUFJoAR2ZVrDy1vF3D07"

                /**
                 * Best-effort supplement for the hourly fields the China API
                 * does not provide. Runs in parallel with the weather request
                 * and is silently dropped if it fails or times out.
                 */
                val supplementDeferred = async {
                    runCatching {
                        supplementApi.fetch(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            // "auto" lets Open-Meteo derive the zone from the
                            // coordinates; a malformed stored zone would make
                            // the request fail with 400.
                            timezone = "auto"
                        )
                    }.getOrNull()?.takeIf { it.isSuccessful }?.body()
                }

                val response = safeApiCall {
                    api.getLocationKey(location.latitude, location.longitude)
                }.getOrThrow()

                val locationKey = response[0].locationKey ?: response[0].key
                ?: throw AppException.EmptyResponseBody()

                val forecastResponse = safeApiCall {
                    api.getForecast(
                        location.latitude,
                        location.longitude,
                        appKey = appKey,
                        sign = sign,
                        locationKey = locationKey
                    )
                }.getOrThrow()

                lastForecast = forecastResponse

                val domain = forecastResponse.toDomain(location)

                val merged = domain.withOpenMeteoSupplement(supplementDeferred.await())

                WeatherDataPack(merged)
            }

            override suspend fun saveToDb(data: WeatherDataPack, cacheModel: CacheModel) {
                useGenericSaveImplementationForWeather(
                    existingHourly = cacheModel.cachedHourly,
                    data.weather,
                    weatherDao
                )
            }

            override fun finishedResult(data: Weather): FinishedWeatherResult {
                return FinishedWeatherResult(weather = data)
            }
        }
    }

    override fun airQualityCapability(): AirQualityCapability? {
        return object : AirQualityCapability {
            override suspend fun fetchAndProcess(
                location: Location,
                isManualRefresh: Boolean,
                isForceRefresh: Boolean,
                airQualityCacheModel: AirQualityCacheModel
            ): AirQualityDataPack {
                val airQuality = lastForecast?.toAirQuality()
                    ?: throw AppException.EmptyResponseBody()

                return AirQualityDataPack(airQuality = airQuality, location = location)
            }

            override suspend fun saveToDb(data: AirQualityDataPack) {
                useGenericSaveImplementationForAirQuality(airQualityDao, data)
            }

            override fun finishedResult(data: AirQuality): FinishedAirQualityResult {
                return FinishedAirQualityResult(airQuality = data)
            }
        }
    }

    override fun alertCapability(): AlertCapability? {
        return object : AlertCapability {
            override suspend fun fetchAndProcess(
                location: Location,
                isManualRefresh: Boolean,
                isForceRefresh: Boolean,
                alertCacheModel: AlertCacheModel
            ): AlertsDataPack {
                val alerts = lastForecast?.alerts.orEmpty().toDomain(location.id)

                return AlertsDataPack(alerts = alerts, location = location)
            }

            override suspend fun saveToDb(
                data: AlertsDataPack,
                alertCacheModel: AlertCacheModel
            ) {
                useGenericSaveImplementationForAlerts(data, alertsDao, dao)
            }

            override fun finishedResult(data: List<Alert>): FinishedAlertsResult {
                return FinishedAlertsResult(alerts = data)
            }
        }
    }
}