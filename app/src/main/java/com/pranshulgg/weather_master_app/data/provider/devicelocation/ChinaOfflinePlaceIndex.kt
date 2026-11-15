package com.pranshulgg.weather_master_app.data.provider.devicelocation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Offline reverse lookup for Chinese place names.
 *
 * Remote reverse geocoding (Nominatim) is unreliable and frequently unreachable in
 * mainland China, which used to leave the header showing a raw coordinate pair.
 * This index ships a compact list of administrative centres (province + prefecture
 * level units) generated from public boundary data, so a nearby name can be resolved
 * locally without any network call.
 */
@Singleton
class ChinaOfflinePlaceIndex @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private data class Place(
        val name: String,
        val province: String,
        val latitude: Double,
        val longitude: Double
    )

    private val places: List<Place> by lazy { loadPlaces() }

    /** Prefecture-level (and district) entries; province centroids overlap them, so they win. */
    private val detailedPlaces: List<Place> by lazy { places.filter { it.province.isNotBlank() } }

    /** Province / municipality centroids, used only when nothing finer is near. */
    private val provincePlaces: List<Place> by lazy { places.filter { it.province.isBlank() } }

    /**
     * @return a display name close enough to the given coordinates, or null when
     * nothing is within [MAX_DISTANCE_KM] (for example an offshore point, or an
     * area the bundled data does not cover).
     */
    fun nameFor(latitude: Double, longitude: Double): String? {
        // Prefer the finest match: a province centroid usually sits on top of its
        // capital, and "山西省" would win over "太原市" otherwise.
        nearestWithin(detailedPlaces, latitude, longitude)?.let { return displayName(it) }
        nearestWithin(provincePlaces, latitude, longitude)?.let { return displayName(it) }
        return null
    }

    private fun nearestWithin(
        candidates: List<Place>,
        latitude: Double,
        longitude: Double
    ): Place? {
        var best: Place? = null
        var bestDistance = Double.MAX_VALUE

        for (place in candidates) {
            val distance = distanceKm(latitude, longitude, place.latitude, place.longitude)
            if (distance < bestDistance) {
                bestDistance = distance
                best = place
            }
        }

        if (bestDistance > MAX_DISTANCE_KM) return null
        return best
    }

    private fun displayName(place: Place): String {
        // Municipalities / SARs are one city made of districts, so keep the parent
        // name: "朝阳区" alone would be ambiguous.
        val parentIsCity = place.province.endsWith("市") && place.province != place.name
        return if (parentIsCity) "${place.province} ${place.name}" else place.name
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Equirectangular approximation is plenty for picking the nearest centre.
        val meanLat = Math.toRadians((lat1 + lat2) / 2.0)
        val x = Math.toRadians(lon2 - lon1) * cos(meanLat)
        val y = Math.toRadians(lat2 - lat1)
        return sqrt(x * x + y * y) * EARTH_RADIUS_KM
    }

    private fun loadPlaces(): List<Place> = runCatching {
        val raw = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val name = item.optString("n").takeIf { it.isNotBlank() } ?: continue
                val latitude = item.optDouble("la", Double.NaN)
                val longitude = item.optDouble("lo", Double.NaN)
                if (latitude.isNaN() || longitude.isNaN()) continue
                add(
                    Place(
                        name = name,
                        province = item.optString("p"),
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val ASSET_NAME = "cn_places.json"
        const val EARTH_RADIUS_KM = 6371.0

        /** Anything further away is not "the place you are in". */
        const val MAX_DISTANCE_KM = 100.0
    }
}