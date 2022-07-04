package com.addi.business.evaluator

import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.LeadEvaluator
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.ProspectQualifier

/**
 * This performs the following evaluation:
 *
 * Our internal prospect qualification system gives a satisfactory score for that
 * person. This system outputs a random score between 0 and 100. A lead could
 * be turned into prospect if the score is greater than 60.
 */
class ScoreQualificationEvaluator(
    private val prospectQualifier: ProspectQualifier,
    private val minimumScore: Int = DEFAULT_MINIMUM_SCORE
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome {
        val qualification = prospectQualifier.getScore(GetProspectQualificationCommand(command.nationalIdNumber))
        if (qualification.score <= minimumScore) {
            return EvaluationOutcome.fail("lead score is below minimum score of '$minimumScore'");
        }

        return EvaluationOutcome.success()
    }


    companion object {
        const val DEFAULT_MINIMUM_SCORE = 60
    }
}