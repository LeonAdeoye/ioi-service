package com.leon.service

import com.leon.model.Ioi
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class IoiBookService
{
    private val created = ConcurrentHashMap<UUID, Ioi>()

    fun add(ioi: Ioi)
    {
        created[ioi.requestId] = ioi.copy(status = "LIVE", cancelReason = null)
    }

    fun get(requestId: UUID): Ioi?
    {
        return created[requestId]
    }

    fun getAll(): List<Ioi>
    {
        return created.values.toList()
    }

    fun getLive(): List<Ioi>
    {
        return created.values.filter { it.status == "LIVE" }.distinctBy { it.requestId }
    }

    fun getManuallyCancelled(): List<Ioi>
    {
        return created.values.filter { it.status == "CANCELLED" && it.cancelReason == MANUAL }
    }

    fun markCancelled(requestId: UUID, manual: Boolean = false): Ioi?
    {
        val current = created[requestId] ?: return null
        if (manual)
        {
            val cancelled = current.copy(status = "CANCELLED", cancelReason = MANUAL)
            created[requestId] = cancelled
            return cancelled
        }

        created.remove(requestId)
        return current.copy(status = "CANCELLED")
    }

    fun clearLive()
    {
        created.entries.removeIf { it.value.status == "LIVE" }
    }

    fun clear()
    {
        created.clear()
    }

    companion object
    {
        const val MANUAL = "MANUAL"
    }
}
