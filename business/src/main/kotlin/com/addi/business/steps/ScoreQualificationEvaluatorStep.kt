package com.addi.business.steps

import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.domain.evaluator.LeadEvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.domain.evaluator.LeadEvaluationBucket.PERSON_HAS_SCORE_QUALIFICATION
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.core.EvaluatorStep
import com.addi.evaluator.domain.PipelineParameters
import com.addi.business.adapter.ProspectQualifier

/**
 * This performs the following evaluation:
 *
 * Our internal prospect qualification system gives a satisfactory score for that
 * person. This system outputs a random score between 0 and 100. A lead could
 * be turned into prospect if the score is greater than 60.
 */
class ScoreQualificationEvaluatorStep(
    private val prospectQualifier: ProspectQualifier,
    private val minimumScore: Int = DEFAULT_MINIMUM_SCORE
) : EvaluatorStep {
    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome {
        try {
            val qualification = prospectQualifier.getScore(GetProspectQualificationCommand(parameters.get(NATIONAL_ID_NUMBER)))
            if (qualification.score <= minimumScore) {
                return EvaluationOutcome.fail("lead score '${qualification.score}' returned is below minimum score of '$minimumScore'");
            }

            return EvaluationOutcome.success(
                mapOf(
                    PERSON_HAS_SCORE_QUALIFICATION to "true"
                )
            )
        } catch (ex: Exception) {
            return EvaluationOutcome.fail(ex)
        }
    }

    companion object {
        const val DEFAULT_MINIMUM_SCORE = 60
    }
}