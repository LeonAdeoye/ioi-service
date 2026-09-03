package com.leon.disruptors

class DisruptorPayload(val payloadType: String, val payload: String)
{
    constructor(payload: String) : this("", payload)

    override fun toString(): String
    {
        return "DisruptorPayload{payloadType='$payloadType', payload='$payload'}"
    }
}
