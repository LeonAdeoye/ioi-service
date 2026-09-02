package com.leon.rule

import com.leon.config.IoiProperties
import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "MinQuantity", description = "No IOI below min shares unless notional exceeds the USD minimum", priority = 8)
class MinQuantityRule(private val properties: IoiProperties)
{
    @Condition
    fun isTooSmall(@Fact("request") request: IoiRequest, @Fact("notionalUsd") notionalUsd: Double?): Boolean
    {
        val notional = notionalUsd ?: 0.0
        return request.quantity < properties.rules.minQuantity && notional <= properties.rules.minNotionalUsd
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for orders below ${properties.rules.minQuantity} shares unless more than ${properties.rules.minNotionalUsd.toLong()} notional")
    }
}
