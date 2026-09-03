package com.leon.disruptors

import com.lmax.disruptor.BusySpinWaitStrategy
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import com.lmax.disruptor.util.DaemonThreadFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Scope("prototype")
@Service
class DisruptorServiceImpl : DisruptorService
{
    private val logger = LoggerFactory.getLogger(DisruptorServiceImpl::class.java)
    private var name: String = ""
    private var counter = 0L
    private var disruptor: Disruptor<DisruptorEvent>? = null
    private var producer: DisruptorEventProducer? = null
    private var hasStarted = false
    private var start: Instant? = null

    @Value("\${disruptor.buffer.size:4096}")
    private var bufferSize: Int = 4096

    override fun start(name: String, journalHandler: EventHandler<DisruptorEvent>, actionEventHandler: EventHandler<DisruptorEvent>)
    {
        this.name = name
        counter = 0
        val factory = DisruptorEventFactory()
        val created = Disruptor(factory, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, BusySpinWaitStrategy())
        logger.info("Created {} disruptor with buffer size: {}", name, bufferSize)
        created.handleEventsWith(journalHandler, actionEventHandler)
        created.start()
        logger.info("Started {} disruptor.", name)
        producer = DisruptorEventProducer(created.ringBuffer)
        disruptor = created
        hasStarted = true
        logger.info("Instantiated producer for {} disruptor.", name)
    }

    @Scheduled(cron = "0 */10 * * * *")
    fun logTelemetry()
    {
        if (!hasStarted)
            return

        val ringBuffer = disruptor?.ringBuffer ?: return
        if (ringBuffer.remainingCapacity() != bufferSize.toLong())
        {
            logger.debug("{} Ring buffer's current depth: {}", name, ringBuffer.cursor - ringBuffer.minimumGatingSequence)
            logger.debug("{} Ring buffer's remaining capacity: {}", name, ringBuffer.remainingCapacity())
        }
    }

    override fun push(payLoad: DisruptorPayload)
    {
        if (counter++ == 0L)
            start = Instant.now()

        producer?.onData(payLoad)
    }

    override fun stop()
    {
        if (!hasStarted)
            return

        hasStarted = false
        val end = Instant.now()
        logger.info("start:{} end:{}", start, end)
        disruptor?.halt()
        logger.info("Halted {} disruptor", name)
        disruptor?.shutdown()
        logger.info("Shutdown {} disruptor", name)
    }
}
