package com.leon.service

import com.leon.model.LiveIoi
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class LiveIoiRegistry
{
    private val live = ConcurrentHashMap<UUID, LiveIoi>()
    private val pendingAfterLunch = ConcurrentHashMap<UUID, LiveIoi>()

    fun register(ioi: LiveIoi)
    {
        live[ioi.request.requestId] = ioi
    }

    fun removeLive(requestId: UUID): LiveIoi?
    {
        return live.remove(requestId)
    }

    fun snapshotLive(): List<LiveIoi>
    {
        return live.values.toList()
    }

    fun snapshotPendingAfterLunch(): List<LiveIoi>
    {
        return pendingAfterLunch.values.toList()
    }

    fun moveLiveToPendingAfterLunch(ioi: LiveIoi)
    {
        live.remove(ioi.request.requestId)
        pendingAfterLunch[ioi.request.requestId] = ioi
    }

    fun removePendingAfterLunch(requestId: UUID): LiveIoi?
    {
        return pendingAfterLunch.remove(requestId)
    }
}
