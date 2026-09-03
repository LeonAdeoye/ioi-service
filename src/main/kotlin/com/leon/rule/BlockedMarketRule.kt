package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.service.IoiBlockingService
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "BlockedMarket", description = "Block IOIs for blocked markets", priority = -1)
class BlockedMarketRule
{
    @Condition
    fun isBlocked(@Fact("request") request: IoiRequest, @Fact("blockingService") blockingService: IoiBlockingService): Boolean
    {
        return blockingService.getBlockedMarkets().contains(request.originalMarket)
    }

    @Action
    fun reject(@Fact("request") request: IoiRequest, @Fact("evaluation") evaluation: RuleEvaluation, @Fact("blockingService") blockingService: IoiBlockingService)
    {
        val reason = "Blocked: market '${request.originalMarket}' is blocked"
        blockingService.recordBlockedMarket(request, reason)
        evaluation.reject(reason)
    }
}
