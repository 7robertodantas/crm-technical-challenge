package com.addi.business.service

import com.addi.evaluator.domain.PipelineParameters
import com.addi.business.adapter.PersonRepository
import com.addi.business.steps.JudicialRecordsEvaluatorStep
import com.addi.business.steps.NationalRegistryEvaluatorStep
import com.addi.business.steps.ScoreQualificationEvaluatorStep
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.core.ParallelPipelineStep
import com.addi.evaluator.core.SequentialPipelineStep
import com.addi.business.adapter.JudicialRecordArchive
import com.addi.business.adapter.NationalRegistry
import com.addi.business.adapter.ProspectQualifier
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