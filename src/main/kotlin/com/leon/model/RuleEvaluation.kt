package com.leon.model

class RuleEvaluation
{
    var approved: Boolean = true
    var blocked: Boolean = false
    var reason: String? = null

    fun reject(reason: String)
    {
        if (approved)
        {
            approved = false
            this.reason = reason
        }
    }

    fun rejectBlocked(reason: String)
    {
        reject(reason)
        blocked = true
    }
}
