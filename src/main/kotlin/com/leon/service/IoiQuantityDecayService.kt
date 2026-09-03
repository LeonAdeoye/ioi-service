package com.leon.service

import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class IoiQuantityDecayService
{
    fun nextQuantity(originalQuantity: Long, currentQuantity: Long, lifetimeMinutes: Long, openMinutes: Long): Long
    {
        if (currentQuantity <= 0L || originalQuantity <= 0L)
            return 0L

        if (openMinutes <= 0L || lifetimeMinutes <= 0L)
            return currentQuantity

        val slice = round(originalQuantity.toDouble() * lifetimeMinutes.toDouble() / openMinutes.toDouble()).toLong()
        val deduction = if (slice < 1L) 1L else slice
        return (currentQuantity - deduction).coerceAtLeast(0L)
    }
}
