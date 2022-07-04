package com.addi.business.service

import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.thirdparty.adapter.PersonRepository
import com.addi.business.evaluator.JudicialRecordsEvaluator
import com.addi.business.evaluator.NationalRegistryEvaluator
import com.addi.business.evaluator.ScoreQualificationEvaluator
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.evaluator.core.ParallelEvaluator
import com.addi.business.evaluator.core.SequentialEvaluator
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.ProspectQualifier

class LeadProspectServiceImpl(
    nationalRegistry: NationalRegistry,
    personRepository: PersonRepository,
    judicialRecordArchive: JudicialRecordArchive,
    prospectQualifier: ProspectQualifier
): LeadProspectService {

    /**
     * This instantiates an evaluator that will evaluate
     * in the following order:
     *
     * |----------------------------- sequential evaluator -------------------------|
     * |    |------ parallel evaluator -----| |-------------------------------|     |
     * |    |                               | |                               |     |
     * |    |       (national registry)     | |     (score qualification)     |     |
     * |    |       (judicial records)      | |                               |     |
     * |    |                               | |                               |     |
     * |    |--------------[0]--------------| |--------------[1]--------------|     |
     * |----------------------------------------------------------------------------|
     */
    private val evaluator = SequentialEvaluator(
        ParallelEvaluator(
            NationalRegistryEvaluator(nationalRegistry, personRepository),
            JudicialRecordsEvaluator(judicialRecordArchive)
        ),
        ScoreQualificationEvaluator(
            prospectQualifier
        )
    )

    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome {
        return evaluator.evaluate(command)
    }

}