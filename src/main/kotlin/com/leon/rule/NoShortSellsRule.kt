package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.model.Side
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoShortSells", description = "No IOI for short sells", priority = 2)
class NoShortSellsRule
{
    @Condition
    fun isShortSell(@Fact("request") request: IoiRequest): Boolean
    {
        return request.side == Side.SHORT_SELL
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for short sells")
    }
}
