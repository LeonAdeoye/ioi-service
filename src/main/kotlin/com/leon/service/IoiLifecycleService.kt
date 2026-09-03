package com.leon.service

import com.leon.model.Ioi
import com.leon.model.IoiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class IoiLifecycleService(
    private val ioiBookService: IoiBookService,
    private val liveIoiRegistry: LiveIoiRegistry,
    private val fixIoiMessageBuilder: FixIoiMessageBuilder,
    private val ampsPublisherService: AmpsPublisherService
)
{
    private val logger = LoggerFactory.getLogger(IoiLifecycleService::class.java)

    fun cancel(requestId: UUID): Boolean
    {
        val ioi = ioiBookService.get(requestId)
        val live = liveIoiRegistry.removeLive(requestId)
        val pending = liveIoiRegistry.removePendingAfterLunch(requestId)
        val request = live?.request ?: pending?.request ?: ioi?.let { toRequest(it) }

        if (request == null)
        {
            logger.warn("No IOI found to cancel: {}", requestId)
            return false
        }

        publishCancel(request)
        ioiBookService.markCancelled(requestId)
        logger.info("Cancelled IOI {}", requestId)
        return true
    }

    fun deleteAll(): Int
    {
        val live = liveIoiRegistry.snapshotLive()
        val pending = liveIoiRegistry.snapshotPendingAfterLunch()
        val bookLive = ioiBookService.getLive()
        val cancelledIds = mutableSetOf<UUID>()

        live.forEach { ioi ->
            publishCancel(ioi.request)
            cancelledIds.add(ioi.request.requestId)
        }
        pending.forEach { ioi ->
            cancelledIds.add(ioi.request.requestId)
        }
        bookLive.forEach { ioi ->
            if (cancelledIds.add(ioi.requestId))
                publishCancel(toRequest(ioi))
        }

        liveIoiRegistry.clearAll()
        ioiBookService.clear()
        logger.info("Deleted all IOIs; cancelled {}", cancelledIds.size)
        return cancelledIds.size
    }

    private fun publishCancel(request: IoiRequest)
    {
        val fixMessage = fixIoiMessageBuilder.buildCancel(request)
        ampsPublisherService.publishFixIoi(request.requestId.toString(), request.ric, fixMessage)
    }

    private fun toRequest(ioi: Ioi): IoiRequest
    {
        return IoiRequest(
            ric = ioi.ric,
            trader = ioi.trader,
            quantity = ioi.quantity,
            side = ioi.side,
            price = ioi.price,
            clientIds = ioi.clientIds,
            BloombergQualifier = ioi.BloombergQualifier,
            timestamp = ioi.timestamp,
            lifeTimeInMinutes = ioi.lifeTimeInMinutes,
            comment = ioi.comment,
            requestId = ioi.requestId,
            ioiFlags = ioi.ioiFlags,
            originalMarket = ioi.originalMarket,
            originalOrderType = ioi.originalOrderType,
            source = ioi.source
        )
    }
}
