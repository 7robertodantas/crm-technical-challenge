package com.addi.business.evaluator

import com.addi.business.command.GetProspectQualificationCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.LeadEvaluationOutcome
import com.addi.business.thirdparty.adapter.ProspectQualifier

class ScoreQualificationEvaluator(
    private val prospectQualifier: ProspectQualifier
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome {
        val qualification = prospectQualifier.getScore(GetProspectQualificationCommand(command.nationalIdNumber))
        if (qualification.score <= 60) {
            return LeadEvaluationOutcome.fail("lead score is below 60");
        }

        return LeadEvaluationOutcome.success()
    }
}