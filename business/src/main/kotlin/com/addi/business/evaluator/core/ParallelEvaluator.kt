package com.addi.business.evaluator.core

import com.addi.business.domain.command.LeadEvaluateCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
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
class ParallelEvaluator(
    private val evaluators: List<LeadEvaluator>,
    private val coroutineContext: CoroutineContext = Dispatchers.Default
) : LeadEvaluator {

    constructor(
        vararg evaluators: LeadEvaluator): this(evaluators.toList())

    constructor(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        vararg evaluators: LeadEvaluator): this(evaluators.toList(), coroutineContext)

    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome = withContext(coroutineContext) {
        evaluators
            .map { evaluator -> async { evaluator.evaluate(command) } }
            .awaitAll()
            .reduce(EvaluationOutcome::combine)
    }
}