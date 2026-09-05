package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.RuleEvaluation
import com.leon.service.IoiBlockingService
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "BlockedStock", description = "Block IOIs for blocked stocks", priority = -2)
class BlockedStockRule
{
    @Condition
    fun isBlocked(@Fact("request") request: IoiRequest, @Fact("blockingService") blockingService: IoiBlockingService): Boolean
    {
        return blockingService.getBlockedStocks().contains(request.ric)
    }

    @Action
    fun reject(@Fact("request") request: IoiRequest, @Fact("evaluation") evaluation: RuleEvaluation, @Fact("blockingService") blockingService: IoiBlockingService)
    {
        val reason = "Blocked: stock '${request.ric}' is blocked"
        blockingService.recordBlockedStock(request, reason)
        evaluation.rejectBlocked(reason)
    }
}
