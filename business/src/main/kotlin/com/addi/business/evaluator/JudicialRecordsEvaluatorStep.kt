package com.addi.business.evaluator

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.evaluator.core.PipelineParameters
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.evaluator.core.EvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.evaluator.core.EvaluationBucket.PERSON_HAS_JUDICIAL_RECORDS
import com.addi.business.evaluator.core.EvaluatorStep
import com.addi.business.thirdparty.adapter.JudicialRecordArchive

/**
 * This performs the following evaluation:
 *
 * The person does not have any judicial records in the national archives'
 * external system.
 */
class JudicialRecordsEvaluatorStep(
    private val judicialRecordArchive: JudicialRecordArchive
) : EvaluatorStep {
    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome {
        try {
            val judicial = judicialRecordArchive.getRegistry(GetPersonDataCommand(parameters.get(NATIONAL_ID_NUMBER)))
            if (judicial.hasRecords) {
                return EvaluationOutcome.fail("person has judicial records")
            }

            return EvaluationOutcome.success(
                mapOf(
                    PERSON_HAS_JUDICIAL_RECORDS to "true"
                )
            )
        } catch (ex: PersonNotFoundException) {
            return EvaluationOutcome.fail("failed to fetch person judicial records")
        } catch (ex: Exception) {
            return EvaluationOutcome.fail(ex)
        }
    }
}