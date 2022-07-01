package com.addi.business.evaluator.core

import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.EvaluationOutcome
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ParallelEvaluator(
    private val evaluators: List<LeadEvaluator>
) : LeadEvaluator {

    constructor(vararg evaluators: LeadEvaluator): this(evaluators.toList())

    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome = coroutineScope {
        evaluators
            .map { evaluator -> async { evaluator.evaluate(command) } }
            .awaitAll()
            .reduce(EvaluationOutcome::combine)
    }
}