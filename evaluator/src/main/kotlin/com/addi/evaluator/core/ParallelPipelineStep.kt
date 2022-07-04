package com.addi.evaluator.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * This can be used to compose evaluators
 * that will execute all in parallel.
 *
 * It won't fail fast - meaning that all evaluators will be executed regardless of their outcomes.
 *
 * After all evaluators returns their outcomes, the result will then be combined together
 * and the first failure (at any order) will be picked.
 *
 * If all evaluators succeeded, then the outcome will also be a succeeded outcome.
 *
 * Given a list of 4 evaluators.
 *
 * e.g.
 *      success | success | fail("some reason") | success   should return fail("some reason")
 *      success | success | success | success               should return success
 */
class ParallelPipelineStep(
    private val steps: List<EvaluatorStep>,
    private val coroutineContext: CoroutineContext = Dispatchers.Default
) : EvaluatorStep {

    constructor(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        vararg steps: EvaluatorStep
    ): this(steps.toList(), coroutineContext)

    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome = withContext(coroutineContext) {
        steps
            .map { evaluator -> async {
                val logger = LoggerFactory.getLogger(evaluator.javaClass)
                logger.info("Evaluating (async) $parameters")
                evaluator.evaluate(parameters)
            } }
            .awaitAll()
            .reduce(EvaluationOutcome::combine)
    }
}