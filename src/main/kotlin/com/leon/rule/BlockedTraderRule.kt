package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.service.IoiBlockingService
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "BlockedTrader", description = "Block IOIs from blocked traders", priority = -3)
class BlockedTraderRule
{
    @Condition
    fun isBlocked(@Fact("request") request: IoiRequest, @Fact("blockingService") blockingService: IoiBlockingService): Boolean
    {
        return blockingService.getBlockedTraders().contains(request.trader)
    }

    @Action
    fun reject(@Fact("request") request: IoiRequest, @Fact("evaluation") evaluation: RuleEvaluation, @Fact("blockingService") blockingService: IoiBlockingService)
    {
        val reason = "Blocked: trader '${request.trader}' is blocked"
        blockingService.recordBlockedTrader(request, reason)
        evaluation.reject(reason)
    }
}
