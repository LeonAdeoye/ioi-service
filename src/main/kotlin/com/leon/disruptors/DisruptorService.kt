package com.leon.disruptors

import com.lmax.disruptor.EventHandler

interface DisruptorService
{
    fun start(name: String, journalHandler: EventHandler<DisruptorEvent>, actionEventHandler: EventHandler<DisruptorEvent>)
    fun push(payLoad: DisruptorPayload)
    fun stop()
}
