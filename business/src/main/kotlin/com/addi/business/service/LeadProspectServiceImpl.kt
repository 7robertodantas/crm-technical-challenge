package com.addi.business.service

import com.addi.business.evaluator.core.PipelineParameters
import com.addi.business.thirdparty.adapter.PersonRepository
import com.addi.business.evaluator.JudicialRecordsEvaluatorStep
import com.addi.business.evaluator.NationalRegistryEvaluatorStep
import com.addi.business.evaluator.ScoreQualificationEvaluatorStep
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.evaluator.core.ParallelPipelineStep
import com.addi.business.evaluator.core.SequentialPipelineStep
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.ProspectQualifier
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class LeadProspectServiceImpl(
    nationalRegistry: NationalRegistry,
    personRepository: PersonRepository,
    judicialRecordArchive: JudicialRecordArchive,
    prospectQualifier: ProspectQualifier,
    coroutineContext: CoroutineContext = Dispatchers.Default
): LeadProspectService {

    private val nationalRegistryStep = NationalRegistryEvaluatorStep(nationalRegistry, personRepository)
    private val judicialRecordsStep = JudicialRecordsEvaluatorStep(judicialRecordArchive)
    private val scoreQualificationStep  = ScoreQualificationEvaluatorStep(prospectQualifier)
    private val nationalRegistryAndJudicialStep = ParallelPipelineStep(
        coroutineContext = coroutineContext,
        steps = listOf(
            nationalRegistryStep,
            judicialRecordsStep
        )
    )

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
    private val evaluator = SequentialPipelineStep(
        steps = listOf(
            nationalRegistryAndJudicialStep,
            scoreQualificationStep
        )
    )

    /**
     * Performs all validations and returns a single evaluation outcome with the result.
     */
    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome {
        return evaluator.evaluate(parameters)
    }

}