package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.Ioi
import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.model.RuleEvaluation
import com.leon.rule.BlockedMarketRule
import com.leon.rule.BlockedStockRule
import com.leon.rule.BlockedTraderRule
import com.leon.rule.MinAdvPercentageRule
import com.leon.rule.MinNotionalRule
import com.leon.rule.MinQuantityRule
import com.leon.rule.NoAfterHoursRule
import com.leon.rule.NoIocRule
import com.leon.rule.NoLunchBreakRule
import com.leon.rule.NoMarketOrdersRule
import com.leon.rule.NoShortSellsRule
import com.leon.rule.NoStopLossRule
import com.leon.rule.PriceDeviationRule
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngineParameters
import org.jeasy.rules.core.DefaultRulesEngine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RulesEngine(
    private val properties: IoiProperties,
    private val pricingServiceClient: PricingServiceClient,
    private val exchangeServiceClient: ExchangeServiceClient,
    private val marketHoursService: MarketHoursService,
    private val fixIoiMessageBuilder: FixIoiMessageBuilder,
    private val ampsPublisherService: AmpsPublisherService,
    private val ioiStatsService: IoiStatsService,
    private val ioiBlockingService: IoiBlockingService
)
{
    private val logger = LoggerFactory.getLogger(RulesEngine::class.java)

    fun processIoiRequest(request: IoiRequest): IoiResponse
    {
        logger.info("Processing IOI request {} for {} qty={} side={}", request.requestId, request.ric, request.quantity, request.side)

        val evaluation = evaluateRules(request)

        return if (evaluation.approved)
        {
            val ioi = generateIoi(request)
            ampsPublisherService.publishFixIoi(request.requestId.toString(), request.ric, ioi.fixMessage)
            ioiStatsService.recordCreated(request)
            logger.info("Approved IOI request {}", request.requestId)
            IoiResponse(
                requestId = request.requestId.toString(),
                approved = true,
                reason = null
            )
        }
        else
        {
            val reason = evaluation.reason ?: "Request did not meet criteria"
            ioiStatsService.recordUnapproved(request, reason)
            logger.info("Rejected IOI request {}: {}", request.requestId, reason)
            IoiResponse(
                requestId = request.requestId.toString(),
                approved = false,
                reason = reason
            )
        }
    }

    private fun evaluateRules(request: IoiRequest): RuleEvaluation
    {
        val adv = pricingServiceClient.getAdv(request.ric)
        val marketPrice = pricingServiceClient.getClosePrice(request.ric)
        val priceForNotional = request.price ?: marketPrice
        val currency = marketHoursService.currencyFor(request.originalMarket)
        val notionalUsd = if (priceForNotional != null)
            exchangeServiceClient.convertToUsd(request.quantity * priceForNotional, currency)
        else
            null
        val advPercentage = if (adv != null && adv > 0L)
            request.quantity.toDouble() / adv.toDouble() * 100.0
        else
            null

        logger.info("IOI facts for {}: adv={}, adv%={}, marketPrice={}, notionalUsd={}", request.requestId, adv, advPercentage, marketPrice, notionalUsd)

        val evaluation = RuleEvaluation()
        val facts = Facts()
        facts.put("request", request)
        facts.put("evaluation", evaluation)
        facts.put("adv", adv)
        facts.put("advPercentage", advPercentage)
        facts.put("marketPrice", marketPrice)
        facts.put("notionalUsd", notionalUsd)
        facts.put("blockingService", ioiBlockingService)

        val rules = Rules(*emptyArray<Any>())
        rules.register(BlockedTraderRule())
        rules.register(BlockedStockRule())
        rules.register(BlockedMarketRule())
        rules.register(NoMarketOrdersRule())
        rules.register(NoShortSellsRule())
        rules.register(NoStopLossRule())
        rules.register(NoIocRule())
        rules.register(NoLunchBreakRule(marketHoursService))
        rules.register(NoAfterHoursRule(marketHoursService))
        rules.register(MinNotionalRule(properties))
        rules.register(MinQuantityRule(properties))
        rules.register(MinAdvPercentageRule(properties))
        rules.register(PriceDeviationRule(properties))

        val engine = DefaultRulesEngine(RulesEngineParameters().skipOnFirstAppliedRule(true))
        engine.fire(rules, facts)
        return evaluation
    }

    private fun generateIoi(request: IoiRequest): Ioi
    {
        val fixMessage = fixIoiMessageBuilder.build(request)
        return Ioi(
            ric = request.ric,
            trader = request.trader,
            quantity = request.quantity,
            side = request.side,
            price = request.price,
            originalOrderType = request.originalOrderType,
            originalMarket = request.originalMarket,
            clientIds = request.clientIds,
            BloombergQualifier = request.BloombergQualifier,
            timestamp = request.timestamp,
            lifeTimeInMinutes = request.lifeTimeInMinutes,
            comment = request.comment,
            requestId = request.requestId,
            ioiFlags = request.ioiFlags,
            fixMessage = fixMessage
        )
    }
}
