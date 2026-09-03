package com.leon.disruptors

import com.lmax.disruptor.RingBuffer

class DisruptorEventProducer(private val ringBuffer: RingBuffer<DisruptorEvent>)
{
    fun onData(payload: DisruptorPayload)
    {
        val sequence = ringBuffer.next()
        try
        {
            val event = ringBuffer.get(sequence)
            event.payload = payload
        }
        finally
        {
            ringBuffer.publish(sequence)
        }
    }
}
