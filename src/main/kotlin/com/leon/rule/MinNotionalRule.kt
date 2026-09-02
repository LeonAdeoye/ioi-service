package com.leon.rule

import com.leon.config.IoiProperties
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "MinNotional", description = "Notional value must be greater than the configured USD minimum", priority = 7)
class MinNotionalRule(private val properties: IoiProperties)
{
    @Condition
    fun isBelowMinimum(@Fact("notionalUsd") notionalUsd: Double?): Boolean
    {
        if (notionalUsd == null)
            return true
        return notionalUsd <= properties.rules.minNotionalUsd
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("Notional value must be greater than ${properties.rules.minNotionalUsd.toLong()} USD")
    }
}
