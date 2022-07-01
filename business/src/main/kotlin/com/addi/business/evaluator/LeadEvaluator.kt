package com.addi.business.evaluator

import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.LeadEvaluationOutcome

interface LeadEvaluator {
    suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome
}