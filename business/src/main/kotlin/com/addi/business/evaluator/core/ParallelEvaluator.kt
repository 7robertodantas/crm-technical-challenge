package com.addi.business.evaluator.core

import com.addi.business.command.LeadEvaluateCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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