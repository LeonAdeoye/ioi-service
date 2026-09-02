package com.leon.model

class RuleEvaluation
{
    var approved: Boolean = true
    var reason: String? = null

    fun reject(reason: String)
    {
        if (approved)
        {
            approved = false
            this.reason = reason
        }
    }
}
