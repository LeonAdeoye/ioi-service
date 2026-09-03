package com.leon.disruptors

class DisruptorEvent
{
    var payload: DisruptorPayload? = null

    override fun toString(): String
    {
        return "DisruptorEvent{payload=$payload}"
    }
}
