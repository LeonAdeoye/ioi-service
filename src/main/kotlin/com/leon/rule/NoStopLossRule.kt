package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.OrderType
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoStopLoss", description = "No IOI for stop-loss orders", priority = 3)
class NoStopLossRule
{
    @Condition
    fun isStopLoss(@Fact("request") request: IoiRequest): Boolean
    {
        return request.originalOrderType == OrderType.STOP_LOSS
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for stop-loss orders")
    }
}
