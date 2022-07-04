package com.addi.business.evaluator.core

import com.addi.business.domain.command.LeadEvaluateCommand

class SequentialEvaluator(
    private val evaluators: List<LeadEvaluator>
) : LeadEvaluator {

    constructor(vararg evaluators: LeadEvaluator) : this(evaluators.toList())

    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome {
        return evaluators.fold(EvaluationOutcome.success()) { outcome, next ->
            outcome.flatMap { next.evaluate(command) }
        }
    }
}