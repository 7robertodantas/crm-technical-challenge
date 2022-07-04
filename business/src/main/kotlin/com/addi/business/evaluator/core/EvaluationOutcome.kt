package com.addi.business.evaluator.core

import java.lang.Exception

/**
 * This represents an outcome of a lead evaluation.
 *
 * If the converted flag is true, then it is succeeded.
 * If the converted flag is false, then it has failed for some reason.
 *
 * The error parameter may contain additional information about the failure.
 */
data class EvaluationOutcome(
    val converted: Boolean,
    val error: String?
) {

    /**
     * The associative method between two evaluations.
     * If the current is fail then it will return the current.
     * If the current is succeeded it will return the other parameter.
     */
    fun combine(other: EvaluationOutcome): EvaluationOutcome {
        return if (isFail()) this
        else other
    }

    /**
     * Bind method that helps to chain evaluations if the previous is succeeded.
     *
     * This allows us to perform next evaluation only if the current has succeed.
     *
     * e.g.
     *
     * suceeded.flatMap(outcome -> evaluator.evaluate(command))
     *      will execute the function given to the flatmap.
     *
     * failed.flatMap(outcome -> evaluator.evaluate(command))
     *      will return the failed instance.
     */
    suspend fun flatMap(fn: suspend (EvaluationOutcome) -> EvaluationOutcome) : EvaluationOutcome {
        return if (isFail()) this
        else fn(this)
    }

    /**
     * Returns whether this outcome represents a failure.
     */
    fun isFail(): Boolean = !converted && error != null

    /**
     * Returns whether this outcome represents a success.
     */
    fun isSuccess(): Boolean = !isFail()

    companion object {
        fun fail(ex: Exception): EvaluationOutcome =
            EvaluationOutcome(
                converted = false,
                error = "something went wrong. ${ex.message}"
            )

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
