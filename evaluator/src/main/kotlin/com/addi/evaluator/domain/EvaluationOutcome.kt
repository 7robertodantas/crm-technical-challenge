package com.addi.evaluator.domain

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * This represents an outcome of an evaluation.
 *
 * If the converted flag is true, then it is succeeded.
 * If the converted flag is false, then it has failed for some reason.
 *
 * The error parameter may contain additional information about the failure.
 */
data class EvaluationOutcome(
    val success: Boolean,
    val error: String?,
    val parameters: Map<EvaluationBucket, String>
) {

    /**
     * The associative method between two evaluations.
     * If the current is fail then it will return the current.
     * If the current is succeeded it will return the other parameter.
     */
    fun combine(other: EvaluationOutcome): EvaluationOutcome {
        return if (isFail()) this
        else EvaluationOutcome(other.success, other.error, other.parameters + parameters)
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
        return if (isFail()) {
            this
        } else {
            combine(fn(this))
        }
    }

    /**
     * Returns whether this outcome represents a failure.
     */
    @JsonIgnore
    fun isFail(): Boolean = !success

    /**
     * Returns whether this outcome represents a success.
     */
    fun isSuccess(): Boolean = success

    companion object {
        fun fail(ex: Exception): EvaluationOutcome =
            EvaluationOutcome(
                success = false,
                error = "something went wrong. ${ex.message ?: ex.stackTrace.contentToString() }",
                parameters = emptyMap()
            )

        fun fail(error: String, parameters: Map<EvaluationBucket, String> = emptyMap()): EvaluationOutcome =
            EvaluationOutcome(
                success = false,
                error = error,
                parameters = parameters
            )

        fun success(): EvaluationOutcome =
            EvaluationOutcome(
                success = true,
                error = null,
                parameters = emptyMap()
            )

        fun success(parameters: Map<EvaluationBucket, String>) =
            EvaluationOutcome(
                success = true,
                error = null,
                parameters = parameters
            )
    }
}
