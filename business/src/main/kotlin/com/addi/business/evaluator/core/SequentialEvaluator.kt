package com.addi.business.evaluator.core

import com.addi.business.domain.command.LeadEvaluateCommand

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