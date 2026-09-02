package com.leon.rule

import com.leon.config.IoiProperties
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "MinAdvPercentage", description = "Order size must be more than the configured percentage of ADV", priority = 9)
class MinAdvPercentageRule(private val properties: IoiProperties)
{
    @Condition
    fun isBelowAdvThreshold(@Fact("adv") adv: Long?, @Fact("advPercentage") advPercentage: Double?): Boolean
    {
        if (adv == null || adv <= 0L || advPercentage == null)
            return true
        return advPercentage <= properties.rules.minAdvPercentage
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation, @Fact("adv") adv: Long?)
    {
        if (adv == null || adv <= 0L)
            evaluation.reject("ADV not found for instrument; cannot evaluate ADV percentage")
        else
            evaluation.reject("Order size must be more than ${properties.rules.minAdvPercentage}% of ADV")
    }
}
