package com.addi.business.evaluator.core

data class EvaluationOutcome(
    val converted: Boolean,
    val error: String?
) {

    fun combine(other: EvaluationOutcome): EvaluationOutcome {
        return if (isFail()) this
        else other
    }

    suspend fun flatMap(fn: suspend (EvaluationOutcome) -> EvaluationOutcome) : EvaluationOutcome {
        return if (isFail()) this
        else fn(this)
    }

    private fun isFail(): Boolean = !converted && error != null

    companion object {
        fun fail(error: String): EvaluationOutcome =
            EvaluationOutcome(
                converted = false,
                error = error
            )

        fun success(): EvaluationOutcome =
            EvaluationOutcome(
                converted = true,
                error = null
            )
    }
}
