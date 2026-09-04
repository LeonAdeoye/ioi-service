package com.leon.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ioiBlocks")
data class IoiBlock(
    @Id
    val id: String = "",
    val blockType: String = "",
    val value: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
)
{
    companion object
    {
        fun of(blockType: String, value: String, userId: String): IoiBlock
        {
            return IoiBlock(
                id = "$blockType:$value",
                blockType = blockType,
                value = value,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
