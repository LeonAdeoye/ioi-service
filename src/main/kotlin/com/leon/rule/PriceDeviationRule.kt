package com.leon.rule

import com.leon.config.IoiProperties
import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import kotlin.math.abs
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "PriceDeviation", description = "IOI price must be within the configured percentage of current market price", priority = 10)
class PriceDeviationRule(private val properties: IoiProperties)
{
    @Condition
    fun isOutsideBand(@Fact("request") request: IoiRequest, @Fact("marketPrice") marketPrice: Double?): Boolean
    {
        val ioiPrice = request.price
        if (ioiPrice == null || marketPrice == null || marketPrice == 0.0)
            return true

        val deviation = abs(ioiPrice - marketPrice) / marketPrice * 100.0
        return deviation > properties.rules.maxPriceDeviationPercent
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation, @Fact("request") request: IoiRequest, @Fact("marketPrice") marketPrice: Double?)
    {
        if (request.price == null)
            evaluation.reject("IOI price is required for Limit orders")
        else if (marketPrice == null || marketPrice == 0.0)
            evaluation.reject("Market price not found; cannot validate IOI price")
        else
            evaluation.reject("IOI price must be within ${properties.rules.maxPriceDeviationPercent}% of current market price")
    }
}
