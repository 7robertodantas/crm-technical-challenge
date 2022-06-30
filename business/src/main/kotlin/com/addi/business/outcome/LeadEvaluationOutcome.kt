package com.addi.business.outcome

data class LeadEvaluationOutcome(
    val converted: Boolean,
    val error: String?
) {
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
