package com.addi.evaluator.core

import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters

/**
 * Interface that can hold a logic to evaluate something.
 */
interface EvaluatorStep {
    suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome
}