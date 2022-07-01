package com.addi.business.evaluator.core

import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.EvaluationOutcome

interface LeadEvaluator {
    suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome
}