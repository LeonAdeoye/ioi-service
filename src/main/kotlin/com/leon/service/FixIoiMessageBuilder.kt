package com.leon.service

import com.leon.model.IoiRequest
import com.leon.model.Side
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import quickfix.field.IOIQty
import quickfix.field.IOITransType
import quickfix.field.IOIid
import quickfix.field.IOIQualifier
import quickfix.field.Price
import quickfix.field.SenderCompID
import quickfix.field.Symbol
import quickfix.field.TargetCompID
import quickfix.field.Text
import quickfix.field.ValidUntilTime
import quickfix.fix44.IndicationOfInterest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class FixIoiMessageBuilder
{
    private val logger = LoggerFactory.getLogger(FixIoiMessageBuilder::class.java)

    fun build(request: IoiRequest): String
    {
        val side = when (request.side)
        {
            Side.BUY -> quickfix.field.Side(quickfix.field.Side.BUY)
            Side.SELL -> quickfix.field.Side(quickfix.field.Side.SELL)
            Side.SHORT_SELL -> quickfix.field.Side(quickfix.field.Side.SELL_SHORT)
        }

        val message = IndicationOfInterest(
            IOIid(request.requestId.toString()),
            IOITransType(IOITransType.NEW),
            side,
            IOIQty(request.quantity.toString())
        )

        message.set(Symbol(request.ric))
        request.price?.let { message.set(Price(it)) }

        val commentParts = mutableListOf<String>()
        if (request.comment.isNotBlank())
            commentParts.add(request.comment)
        if (request.clientIds.isNotEmpty())
            commentParts.add("clients=${request.clientIds.joinToString(",")}")
        if (commentParts.isNotEmpty())
            message.set(Text(commentParts.joinToString("; ")))

        if (request.BloombergQualifier.isNotBlank())
            message.set(IOIQualifier(request.BloombergQualifier[0]))

        val validUntil = Instant.ofEpochMilli(request.timestamp).plus(request.lifeTimeInMinutes, ChronoUnit.MINUTES)
        message.set(ValidUntilTime(Date.from(validUntil)))

        message.header.setField(SenderCompID("IOI_SERVICE"))
        message.header.setField(TargetCompID("FIX_GATEWAY"))

        val fixMessage = message.toString()
        logger.info("Generated FIX IOI for request {}: {}", request.requestId, fixMessage.replace('\u0001', '|'))
        return fixMessage
    }
}
