package com.addi.business.evaluator

import com.addi.business.command.GetProspectQualificationCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.LeadEvaluationOutcome
import com.addi.business.thirdparty.adapter.ProspectQualifier

class ScoreQualificationEvaluator(
    private val prospectQualifier: ProspectQualifier,
    private val minimumScore: Int = DEFAULT_MINIMUM_SCORE
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome {
        val qualification = prospectQualifier.getScore(GetProspectQualificationCommand(command.nationalIdNumber))
        if (qualification.score <= minimumScore) {
            return LeadEvaluationOutcome.fail("lead score is below minimum score of '$minimumScore'");
        }

        return LeadEvaluationOutcome.success()
    }


    companion object {
        const val DEFAULT_MINIMUM_SCORE = 60
    }
}