package com.leon.rule

import com.leon.model.IoiRequest
import com.leon.model.OrderType
import com.leon.model.RuleEvaluation
import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Rule

@Rule(name = "NoMarketOrders", description = "No IOI for market orders", priority = 1)
class NoMarketOrdersRule
{
    @Condition
    fun isMarketOrder(@Fact("request") request: IoiRequest): Boolean
    {
        return request.originalOrderType == OrderType.MARKET
    }

    @Action
    fun reject(@Fact("evaluation") evaluation: RuleEvaluation)
    {
        evaluation.reject("No IOI for market orders. IOIs can only be created for Limit orders")
    }
}
