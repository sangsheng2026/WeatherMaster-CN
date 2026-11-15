package com.pranshulgg.weather_master_app.data.repository

import android.content.Context
import androidx.room.Transaction
import com.pranshulgg.weather_master_app.core.managers.SourceManager
import com.pranshulgg.weather_master_app.core.model.domain.AppException
import com.pranshulgg.weather_master_app.core.model.domain.airquality.AirQuality
import com.pranshulgg.weather_master_app.core.model.domain.alerts.Alert
import com.pranshulgg.weather_master_app.core.model.domain.location.Location
import com.pranshulgg.weather_master_app.core.model.domain.weather.Weather
import com.pranshulgg.weather_master_app.core.model.domain.weather.WeatherCurrent
import com.pranshulgg.weather_master_app.core.model.sources.Source
import com.pranshulgg.weather_master_app.core.model.weather.openmeteo.OpenMeteoModel
import com.pranshulgg.weather_master_app.core.network.sources.address.nominatim.json.NominatimRepository
import com.pranshulgg.weather_master_app.data.local.dao.airquality.AirQualityDao
import com.pranshulgg.weather_master_app.data.local.dao.alerts.AlertsDao
import com.pranshulgg.weather_master_app.data.local.dao.weather.WeatherContextDao
import com.pranshulgg.weather_master_app.data.local.mapper.airquality.toDomain
import com.pranshulgg.weather_master_app.data.local.mapper.alerts.toDomain
import com.pranshulgg.weather_master_app.data.local.mapper.locations.toDomain
import com.pranshulgg.weather_master_app.data.local.mapper.locations.toEntity
import com.pranshulgg.weather_master_app.data.local.mapper.weather.toCurrentWeatherEntity
import com.pranshulgg.weather_master_app.data.local.mapper.weather.toDomain
import com.pranshulgg.weather_master_app.data.provider.devicelocation.ChinaOfflineGeocoder
import com.pranshulgg.weather_master_app.data.provider.devicelocation.ChinaOfflinePlaceIndex
import com.pranshulgg.weather_master_app.data.provider.devicelocation.DeviceLocation
import com.pranshulgg.weather_master_app.data.provider.devicelocation.GetDeviceLocation
import com.pranshulgg.weather_master_app.data.provider.devicelocation.getCountryCode
import com.pranshulgg.weather_master_app.data.provider.devicelocation.isLocationEnabled
import com.pranshulgg.weather_master_app.feature.intro.toDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.ZoneId
import kotlin.collections.map
import kotlin.coroutines.resumeWithException

class WeatherContextRepository @Inject constructor(
    private val dao: WeatherContextDao,
    private val airQualityDao: AirQualityDao,
    @param:ApplicationContext private val context: Context,
    private val nominatimRepository: NominatimRepository,
    private val alertsDao: AlertsDao,
    private val sourceManager: SourceManager,
    private val chinaOfflineGeocoder: ChinaOfflineGeocoder,
    private val chinaOfflinePlaceIndex: ChinaOfflinePlaceIndex
) {
    private val LOCATION_UPDATE_THRESHOLD_METERS = 1000f // 1000m

    /**
     * Reverse geocoding only affects the displayed name, so it must never sit on the
     * path that the weather refresh waits on. It runs here, off the critical path.
     */
    private val addressResolutionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private companion object {
        /** Hard cap for the address lookup (it is cosmetic, a slow network must not stall us). */
        private const val ADDRESS_TIMEOUT_MS = 5_000L

        /** Hard cap for the country-code lookup, which may hit the platform Geocoder. */
        private const val COUNTRY_CODE_TIMEOUT_MS = 2_000L
    }

    class Callbacks {
        fun onSuccess(cont: CancellableContinuation<DeviceLocation>, result: DeviceLocation) {
            if (cont.isActive) {
                cont.resumeWith(Result.success(result))
            }
        }

        fun onTimeout(cont: CancellableContinuation<DeviceLocation>) {
            if (cont.isActive) {
                cont.resumeWithException(AppException.CurrentLocationUnavailable())
            }
        }
    }

    private val callback = Callbacks()


    fun getLocations(): Flow<List<Location>> {
        return dao.getLocations().map { it.toDomain() }
    }

    suspend fun getLocationsOnce(): List<Location> {
        return dao.getLocationsOnce().map { it.toDomain() }
    }

    @Transaction
    suspend fun deleteLocation(id: String) {
        dao.deleteLocation(id)
        airQualityDao.deleteCurrentAirQuality(id)
    }


    suspend fun updateSourceForLocation(id: String, source: Source) {
        dao.updateSourceForLocation(id, source)
    }

    suspend fun updateOpenMeteoModelForLocation(id: String, model: OpenMeteoModel) {
        dao.updateOpenMeteoModelForLocation(id, model)
    }

    suspend fun updateAirQualitySourceForLocation(id: String, source: Source) {
        dao.updateAirQualitySourceForLocation(id, source)
    }

    suspend fun updateAlertSourceForLocation(id: String, source: Source) {
        dao.updateAlertSourceForLocation(id, source)
    }

    suspend fun getLocationForId(id: String): Location {
        return dao.getLocationForId(id).toDomain()
    }

    suspend fun updateLocationCustomName(id: String, name: String?) {
        dao.updateLocationCustomName(id, name)
    }

    @Transaction
    suspend fun saveLocation(location: Location) {
        val isFirst = dao.getLocationsCount() == 0
        dao.insertWeatherLocation(location.toEntity())

        if (isFirst) {
            updateDefaultLocation(location.id)
        }
    }

    suspend fun isLocationsEmpty(): Boolean {
        val isEmpty = dao.getLocationsCount() == 0
        return isEmpty
    }

    @Transaction
    suspend fun updateDefaultLocation(id: String) {
        dao.clearDefaultLocations()
        dao.updateDefaultLocation(id)
    }

    fun getDefaultLocation(): Flow<Location?> {
        return dao.getDefaultLocation().map { it?.toDomain() }
    }

    suspend fun getWeatherForLocation(locationId: String): Weather? {
        return dao.getWeatherForLocation(locationId).toDomain()
    }

    suspend fun getAirQualityForLocation(locationId: String): AirQuality? {
        return airQualityDao.getAirQualityForLocation(locationId)?.toDomain()
    }

    val getDeviceLocation = GetDeviceLocation()

    /**
     * Refreshes the saved device-location entry if the device has moved far enough.
     * Returns true if the location was actually updated (so callers know to force
     * a fresh weather fetch instead of trusting a now-stale cache).
     */
    suspend fun updateDeviceLocationPosition(): Boolean {

        if (!context.isLocationEnabled()) {

            // Location is not enabled,
            // meaning updating the position should fail.
            // Return false as if the location didn't change.
            return false
        }


        val location =
            suspendCancellableCoroutine { cont ->
                getDeviceLocation.getDeviceLocation(
                    context,
                    onTimeout = {
                        callback.onTimeout(cont)
                    }
                ) { result ->
                    callback.onSuccess(cont, result)
                }
            }


        val newLat = location.latitude ?: return false
        val newLon = location.longitude ?: return false

        val currentLocation = dao.getDeviceLocation()


        val results = FloatArray(1)

        android.location.Location.distanceBetween(
            currentLocation.lat,
            currentLocation.lon,
            newLat,
            newLon,
            results
        )

        val distanceInMeters = results[0]
        // Only update the location if needed
        if (distanceInMeters < LOCATION_UPDATE_THRESHOLD_METERS) {
            return false
        }
        sourceManager.cleanLocationExtras(currentLocation.id)

        // Cheap, local check first (offline China polygon / platform geocoder with a cap).
        val offlineCountryCode = withTimeoutOrNull(COUNTRY_CODE_TIMEOUT_MS) {
            runCatching {
                getCountryCode(context, newLat, newLon, chinaOfflineGeocoder)
            }.getOrNull()
        }

        // Save the position immediately with the name we already have, then let the
        // (possibly unreachable) reverse geocoding catch up in the background.
        // Inside China the name comes from the bundled offline index, so it shows up
        // instantly and without a single network request.
        val offlineName = if (offlineCountryCode == "CN") {
            chinaOfflinePlaceIndex.nameFor(newLat, newLon)
        } else {
            null
        }

        dao.updateDeviceLocation(
            newLat,
            newLon,
            offlineName ?: currentLocation.name,
            currentLocation.country,
            countryCode = offlineCountryCode ?: currentLocation.countryCode ?: "",
            ZoneId.systemDefault().id
        )

        addressResolutionScope.launch {
            val address = withTimeoutOrNull(ADDRESS_TIMEOUT_MS) {
                runCatching {
                    nominatimRepository.getAddress(newLat, newLon)
                }.getOrNull()
            } ?: return@launch

            val city = address.city ?: return@launch

            dao.updateDeviceLocation(
                newLat,
                newLon,
                city,
                address.country,
                countryCode = address.countryCode
                    ?: offlineCountryCode
                    ?: currentLocation.countryCode
                    ?: "",
                ZoneId.systemDefault().id
            )
        }

        return true
    }

    suspend fun saveDeviceLocation() {

        val location = suspendCancellableCoroutine { cont ->
            getDeviceLocation.getDeviceLocation(
                context,
                onTimeout = {
                    callback.onTimeout(cont)
                }) { result ->
                callback.onSuccess(cont, result)
            }
        }
        if (location.latitude == null || location.longitude == null) {
            throw AppException.CurrentLocationUnavailable()
        }


        // First launch: still cap the lookup so an unreachable geocoder cannot freeze setup.
        val address = withTimeoutOrNull(ADDRESS_TIMEOUT_MS) {
            runCatching {
                nominatimRepository.getAddress(
                    location.latitude,
                    location.longitude
                )
            }.getOrNull()
        }


        if (address != null && address.city != null) {

            val countryCode = if (address.countryCode.isNullOrBlank()) {
                getCountryCode(context, location.latitude, location.longitude, chinaOfflineGeocoder)
            } else {
                address.countryCode
            }

            saveLocation(
                location.toDomain(context).copy(
                    name = address.city,
                    country = address.country,
                    countryCode = countryCode
                )
            )
        } else {
            // No remote address: fall back to the bundled offline index so the header
            // shows a real place name instead of a raw coordinate pair.
            val domain = location.toDomain(context)
            val offlineCountryCode = withTimeoutOrNull(COUNTRY_CODE_TIMEOUT_MS) {
                runCatching {
                    getCountryCode(
                        context,
                        location.latitude!!,
                        location.longitude!!,
                        chinaOfflineGeocoder
                    )
                }.getOrNull()
            }
            val offlineName = if (offlineCountryCode == "CN") {
                chinaOfflinePlaceIndex.nameFor(location.latitude, location.longitude)
            } else {
                null
            }

            saveLocation(
                domain.copy(
                    name = offlineName ?: domain.name,
                    countryCode = offlineCountryCode ?: domain.countryCode
                )
            )
        }
    }

    fun getWeatherForTotalLocations(): Flow<List<Weather>> {
        return dao.getWeatherForTotalLocations()
            .map { list -> list.mapNotNull { it.toDomain() } }
    }

    fun getAlertsForTotalLocations(): Flow<List<Alert?>> {
        return alertsDao.getAlertsForTotalLocations().map { list -> list.map { it?.toDomain() } }
    }

    suspend fun getAlertsForLocation(locationId: String): List<Alert?> {
        return alertsDao.getAlertsForLocation(locationId).map { it?.toDomain() }
    }

}


