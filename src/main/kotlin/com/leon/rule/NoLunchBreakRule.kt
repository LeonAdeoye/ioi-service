package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.service.MarketHoursService
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoLunchBreak", description = "No IOI during lunch break", priority = 5)
class NoLunchBreakRule(private val marketHoursService: MarketHoursService)
{
    @Condition
    fun isLunchBreak(@Fact("request") request: IoiRequest): Boolean
    {
        return marketHoursService.isDuringLunch(request.originalMarket, request.timestamp)
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for orders during lunch break")
    }
}
