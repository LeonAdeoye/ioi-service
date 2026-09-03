package com.leon.service

import com.leon.model.ExchangeDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class MarketHoursService(private val domainServiceClient: DomainServiceClient)
{
    private val logger = LoggerFactory.getLogger(MarketHoursService::class.java)
    private val exchangeCache = ConcurrentHashMap<String, ExchangeDto>()

    fun getExchange(acronym: String): ExchangeDto?
    {
        if (acronym.isBlank())
            return null

        val key = acronym.uppercase()
        exchangeCache[key]?.let { return it }

        val fetched = domainServiceClient.getExchangeByAcronym(acronym) ?: return null
        exchangeCache[key] = fetched
        return fetched
    }

    fun isDuringLunch(acronym: String, epochMillis: Long): Boolean
    {
        val exchange = getExchange(acronym) ?: return false
        val lunchStartValue = exchange.lunchStart.orEmpty()
        val lunchEndValue = exchange.lunchEnd.orEmpty()
        if (lunchStartValue.isBlank() || lunchEndValue.isBlank())
            return false

        val localTime = localDateTime(exchange, epochMillis).toLocalTime()
        val lunchStart = parseTime(lunchStartValue) ?: return false
        val lunchEnd = parseTime(lunchEndValue) ?: return false
        return !localTime.isBefore(lunchStart) && localTime.isBefore(lunchEnd)
    }

    fun isAfterHours(acronym: String, epochMillis: Long): Boolean
    {
        val exchange = getExchange(acronym) ?: return false
        val zoned = localDateTime(exchange, epochMillis)
        if (zoned.dayOfWeek == DayOfWeek.SATURDAY || zoned.dayOfWeek == DayOfWeek.SUNDAY)
            return true

        val openValue = exchange.openTime.orEmpty()
        val closeValue = exchange.closeTime.orEmpty()
        if (openValue.isBlank() || closeValue.isBlank())
            return false

        val localTime = zoned.toLocalTime()
        val open = parseTime(openValue) ?: return false
        val close = parseTime(closeValue) ?: return false
        return localTime.isBefore(open) || !localTime.isBefore(close)
    }

    fun currencyFor(acronym: String): String
    {
        val currency = getExchange(acronym)?.currency
        return if (currency.isNullOrBlank()) "USD" else currency
    }

    fun openMinutes(acronym: String): Long
    {
        val exchange = getExchange(acronym) ?: return 0L
        val open = parseTime(exchange.openTime.orEmpty()) ?: return 0L
        val close = parseTime(exchange.closeTime.orEmpty()) ?: return 0L
        var sessionMinutes = Duration.between(open, close).toMinutes()
        if (sessionMinutes <= 0)
            sessionMinutes += Duration.ofHours(24).toMinutes()

        val lunchStart = parseTime(exchange.lunchStart.orEmpty())
        val lunchEnd = parseTime(exchange.lunchEnd.orEmpty())
        val lunchMinutes = if (lunchStart != null && lunchEnd != null)
            Duration.between(lunchStart, lunchEnd).toMinutes().coerceAtLeast(0)
        else
            0L

        return (sessionMinutes - lunchMinutes).coerceAtLeast(0L)
    }

    private fun localDateTime(exchange: ExchangeDto, epochMillis: Long): ZonedDateTime
    {
        val timezone = if (exchange.timezone.isNullOrBlank()) "UTC" else exchange.timezone
        val zone = try
        {
            ZoneId.of(timezone)
        }
        catch (e: Exception)
        {
            logger.warn("Invalid timezone {}, defaulting to UTC", timezone)
            ZoneId.of("UTC")
        }

        return Instant.ofEpochMilli(epochMillis).atZone(zone)
    }

    private fun parseTime(value: String): LocalTime?
    {
        return try
        {
            LocalTime.parse(value)
        }
        catch (e: Exception)
        {
            logger.warn("Invalid time value: {}", value)
            null
        }
    }
}
