package com.pranshulgg.weather_master_app.data.local.mapper.alerts.sources.china

import com.pranshulgg.weather_master_app.core.model.domain.alerts.Alert
import com.pranshulgg.weather_master_app.core.model.weather.alerts.AlertSeverity
import com.pranshulgg.weather_master_app.core.network.sources.weather.china.json.ChinaAlertJson
import com.pranshulgg.weather_master_app.core.utils.extensions.DateTimeExtensions.iso8601TimestampToMilliseconds

/**
 * Maps the official meteorological-bureau alerts returned by the China source.
 * `level` is a Chinese colour code (蓝色/黄色/橙色/红色).
 *
 * Xiaomi returns one entry per alert *publication*, so a re-issued alert
 * ("继续发布") shows up again under a different alertId with identical
 * wording. Those are collapsed to the most recently published one.
 */
fun List<ChinaAlertJson>.toDomain(locationId: String): List<Alert> = latestPerAlert().map { item ->
    Alert(
        locationId = locationId,
        event = listOfNotNull(item.type, item.level)
            .joinToString(" ")
            .ifBlank { item.title ?: "" },
        severity = getSeverity(item.level),
        effective = item.pubTime?.iso8601TimestampToMilliseconds(),
        // Xiaomi does not expose an expiry timestamp for alerts.
        expires = null,
        description = buildDescription(item),
        source = "中国气象台",
        lastUpdatedInMilli = System.currentTimeMillis()
    )
}

private fun List<ChinaAlertJson>.latestPerAlert(): List<ChinaAlertJson> {
    val seen = LinkedHashMap<String, ChinaAlertJson>()

    forEach { item ->
        val key = "${item.type}|${item.level}|${item.title}"
        val existing = seen[key]
        if (existing == null || item.pubTime.orEmpty() >= existing.pubTime.orEmpty()) {
            seen[key] = item
        }
    }

    return seen.values.toList()
}

private fun buildDescription(item: ChinaAlertJson): String = buildString {
    append(item.detail.orEmpty())

    val defense = item.defense?.mapNotNull { it.defenseText?.takeIf(String::isNotBlank) }.orEmpty()
    if (defense.isNotEmpty()) {
        append("\n\n防御指南：\n")
        defense.forEachIndexed { index, text -> append("${index + 1}. $text\n") }
    }
}.trim()

private fun getSeverity(level: String?): AlertSeverity = when (level) {
    "红色" -> AlertSeverity.CRITICAL
    "橙色" -> AlertSeverity.HIGH
    "黄色" -> AlertSeverity.MODERATE
    "蓝色" -> AlertSeverity.LOW
    else -> AlertSeverity.UNKNOWN
}