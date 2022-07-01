package com.addi.business.evaluator

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.LeadEvaluationOutcome
import com.addi.business.thirdparty.adapter.JudicialRecordArchive

class JudicialRecordsEvaluator(
    private val judicialRecordArchive: JudicialRecordArchive
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome {
        val judicial = judicialRecordArchive.getRegistry(GetPersonDataCommand(command.nationalIdNumber))
        if (judicial.hasRecords) {
            return LeadEvaluationOutcome.fail("person has judicial records")
        }

        return LeadEvaluationOutcome.success()
    }
}