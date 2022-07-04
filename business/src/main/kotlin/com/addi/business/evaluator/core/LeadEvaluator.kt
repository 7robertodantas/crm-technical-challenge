package com.addi.business.evaluator.core

import com.addi.business.domain.command.LeadEvaluateCommand

/**
 * Interface that can hold a logic to evaluate a lead.
 */
interface LeadEvaluator {
    suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome
}