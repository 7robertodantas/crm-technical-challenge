package com.addi.business.evaluator

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.LeadEvaluator
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.JudicialRecordArchive

class JudicialRecordsEvaluator(
    private val judicialRecordArchive: JudicialRecordArchive
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome {
        val judicial = judicialRecordArchive.getRegistry(GetPersonDataCommand(command.nationalIdNumber))
        if (judicial.hasRecords) {
            return EvaluationOutcome.fail("person has judicial records")
        }

        return EvaluationOutcome.success()
    }
}