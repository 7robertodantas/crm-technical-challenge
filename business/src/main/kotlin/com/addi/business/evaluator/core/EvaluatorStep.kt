package com.addi.business.evaluator.core

/**
 * Interface that can hold a logic to evaluate something.
 */
interface EvaluatorStep {
    suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome
}