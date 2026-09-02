package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.OrderType
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoIoc", description = "No IOI for IOC orders", priority = 4)
class NoIocRule
{
    @Condition
    fun isIoc(@Fact("request") request: IoiRequest): Boolean
    {
        return request.originalOrderType == OrderType.IOC
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for orders with time-in-force of IOC (Immediate or Cancel)")
    }
}
