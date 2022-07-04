package com.addi.evaluator.core

/**
 * This can be used to compose a chain of evaluators
 * that will execute the evaluate in sequence.
 *
 * It will fail fast - meaning that the chain would stop on first evaluation outcome that returns a fail.
 *
 * Given a list of 4 evaluators.
 *
 * e.g.
 *      success -> success -> fail("some reason")   should return fail("some reason") and won't apply the 4th.
 *      success -> success -> success -> success    should return success
 */
class SequentialPipelineStep(
    private val steps: List<EvaluatorStep>
) : EvaluatorStep {

    constructor(vararg steps: EvaluatorStep) : this(steps.toList())

    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome {
        return steps.fold(EvaluationOutcome.success(parameters.parameters)) { outcome, next ->
            outcome.flatMap { next.evaluate(PipelineParameters(outcome.parameters)) }
        }
    }
}