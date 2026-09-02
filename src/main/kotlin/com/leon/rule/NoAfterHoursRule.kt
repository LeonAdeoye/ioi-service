package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.service.MarketHoursService
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoAfterHours", description = "No IOI after market close or during after-hours trading", priority = 6)
class NoAfterHoursRule(private val marketHoursService: MarketHoursService)
{
    @Condition
    fun isAfterHours(@Fact("request") request: IoiRequest): Boolean
    {
        return marketHoursService.isAfterHours(request.originalMarket, request.timestamp)
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for orders after market close or during after-hours trading")
    }
}
