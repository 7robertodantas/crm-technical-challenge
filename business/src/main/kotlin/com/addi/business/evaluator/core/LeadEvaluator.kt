package com.addi.business.evaluator.core

import com.addi.business.command.LeadEvaluateCommand

interface LeadEvaluator {
    suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome
}