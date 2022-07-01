package com.addi.business.service

import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.evaluator.LeadEvaluator
import com.addi.business.outcome.LeadEvaluationOutcome

class LeadProspectEvaluatorService(
    private val evaluators: List<LeadEvaluator>
) {
    suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome {
        return evaluators.fold(LeadEvaluationOutcome.success()) { outcome, next ->
            outcome.flatMap { next.evaluate(command) }
        }
    }
}