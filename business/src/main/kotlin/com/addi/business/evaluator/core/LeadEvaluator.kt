package com.addi.business.evaluator.core

import com.addi.business.domain.command.LeadEvaluateCommand

interface LeadEvaluator {
    suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome
}