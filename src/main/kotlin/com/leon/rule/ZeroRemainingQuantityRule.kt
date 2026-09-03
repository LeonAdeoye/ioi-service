package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "ZeroRemainingQuantity", description = "No IOI when remaining quantity is zero", priority = 0)
class ZeroRemainingQuantityRule
{
    @Condition
    fun isZeroQuantity(@Fact("request") request: IoiRequest): Boolean
    {
        return request.quantity <= 0L
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI when remaining quantity is zero")
    }
}
