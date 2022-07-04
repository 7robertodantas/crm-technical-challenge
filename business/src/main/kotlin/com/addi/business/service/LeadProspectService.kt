package com.addi.business.service

import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.EvaluationOutcome

/**
 * This represents the business service that will apply the logic to evaluate
 * a lead into prospect.
 */
interface LeadProspectService {

    /**
     * @param command the command that contains relevant data of the lead to process
     * the evaluation to convert into a prospect.
     *
     * @return an evaluation outcome that contains a flag that can determine whether
     * the lead was converted or not, and error if present.
     */
    suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome
}