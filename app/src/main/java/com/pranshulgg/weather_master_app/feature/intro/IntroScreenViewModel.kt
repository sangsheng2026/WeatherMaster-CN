package com.pranshulgg.weather_master_app.feature.intro

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranshulgg.weather_master_app.core.model.domain.AppException
import com.pranshulgg.weather_master_app.core.model.domain.toAppException
import com.pranshulgg.weather_master_app.core.model.domain.toMessageRes
import com.pranshulgg.weather_master_app.core.model.sources.Source
import com.pranshulgg.weather_master_app.core.network.sources.address.nominatim.json.NominatimRepository
import com.pranshulgg.weather_master_app.core.prefs.AppPrefs
import com.pranshulgg.weather_master_app.core.ui.snackbar.SnackbarManager
import com.pranshulgg.weather_master_app.data.backup.BackupRepository
import com.pranshulgg.weather_master_app.data.backup.model.BackupPayload
import com.pranshulgg.weather_master_app.data.provider.devicelocation.ChinaOfflineGeocoder
import com.pranshulgg.weather_master_app.data.provider.devicelocation.ChinaOfflinePlaceIndex
import com.pranshulgg.weather_master_app.data.provider.devicelocation.DeviceLocation
import com.pranshulgg.weather_master_app.data.repository.WeatherContextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class IntroScreenViewModel @Inject constructor(
    val locationsRepo: WeatherContextRepository,
    @ApplicationContext private val context: Context,
    private val nominatimRepository: NominatimRepository,
    private val backupRepository: BackupRepository,
    private val chinaOfflineGeocoder: ChinaOfflineGeocoder,
    private val chinaOfflinePlaceIndex: ChinaOfflinePlaceIndex
) : ViewModel() {

    var isImportingBackup by mutableStateOf(false)
        private set

    fun saveDeviceLocation(location: DeviceLocation) {
        viewModelScope.launch {

            // Reverse geocoding is cosmetic and may be unreachable on some networks,
            // so it must never block saving the location / entering the main screen.
            val address = withTimeoutOrNull(5_000L) {
                runCatching {
                    nominatimRepository.getAddress(location.latitude, location.longitude)
                }.getOrNull()
            }

            // Inside China the global source is often unreachable, so the regional
            // one becomes the default unless the user changes it later.
            val isInChina = location.latitude != null && location.longitude != null &&
                    chinaOfflineGeocoder.checkIsChina(location.latitude, location.longitude)
            val defaultSource = if (isInChina) Source.CHINA else Source.OPEN_METEO

            if (address != null && address.city != null) {
                locationsRepo.saveLocation(
                    location.toDomain(context).copy(
                        name = address.city,
                        country = address.country,
                        countryCode = address.countryCode,
                        source = defaultSource
                    )
                )
            } else {
                // No remote address: use the bundled offline index so the header shows
                // a real place name (e.g. "太原市") instead of a coordinate pair.
                val domain = location.toDomain(context)
                val latitude = location.latitude
                val longitude = location.longitude
                val offlineName = if (
                    latitude != null && longitude != null &&
                    chinaOfflineGeocoder.checkIsChina(latitude, longitude)
                ) {
                    chinaOfflinePlaceIndex.nameFor(latitude, longitude)
                } else {
                    null
                }

                locationsRepo.saveLocation(
                    if (offlineName != null) {
                        domain.copy(
                            name = offlineName,
                            countryCode = "CN",
                            source = defaultSource
                        )
                    } else {
                        domain.copy(source = defaultSource)
                    }
                )
            }

        }

    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            isImportingBackup = true
            try {
                val text = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().decodeToString()
                } ?: throw AppException.BackupFileIOError()

                val payload = Json { ignoreUnknownKeys = true }
                    .decodeFromString(BackupPayload.serializer(), text)

                backupRepository.restoreBackup(payload)
                AppPrefs.initPrefs(context)
                // MainScreen watches the location list reactively and steps past this screen
                // itself once it's non-empty - no explicit navigation needed here.
            } catch (e: SerializationException) {
                SnackbarManager.show(AppException.BackupFileCorrupted().toMessageRes())
            } catch (e: IllegalArgumentException) {
                SnackbarManager.show(AppException.BackupFileCorrupted().toMessageRes())
            } catch (e: IOException) {
                SnackbarManager.show(AppException.BackupFileIOError().toMessageRes())
            } catch (e: Exception) {
                SnackbarManager.show(e.toAppException().toMessageRes())
            } finally {
                isImportingBackup = false
            }
        }
    }

}