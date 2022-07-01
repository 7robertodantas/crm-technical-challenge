package com.addi.business.outcome

data class LeadEvaluationOutcome(
    val converted: Boolean,
    val error: String?
) {

    fun combine(other: LeadEvaluationOutcome): LeadEvaluationOutcome {
        return if (isFail()) this
        else other
    }

    suspend fun flatMap(fn: suspend (LeadEvaluationOutcome) -> LeadEvaluationOutcome) : LeadEvaluationOutcome {
        return if (isFail()) this
        else fn(this)
    }

    private fun isFail(): Boolean = !converted && error != null

    companion object {
        fun fail(error: String): LeadEvaluationOutcome =
            LeadEvaluationOutcome(
                converted = false,
                error = error
            )

        fun success(): LeadEvaluationOutcome =
            LeadEvaluationOutcome(
                converted = true,
                error = null
            )
    }
}
