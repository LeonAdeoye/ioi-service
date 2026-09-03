package com.leon.disruptors

import com.lmax.disruptor.EventFactory

class DisruptorEventFactory : EventFactory<DisruptorEvent>
{
    override fun newInstance(): DisruptorEvent
    {
        return DisruptorEvent()
    }
}
