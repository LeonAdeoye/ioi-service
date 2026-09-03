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
        created[ioi.requestId] = ioi.copy(status = "LIVE")
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
        return created.values.filter { it.status == "LIVE" }
    }

    fun markCancelled(requestId: UUID): Ioi?
    {
        val current = created[requestId] ?: return null
        val cancelled = current.copy(status = "CANCELLED")
        created[requestId] = cancelled
        return cancelled
    }

    fun clear()
    {
        created.clear()
    }
}
