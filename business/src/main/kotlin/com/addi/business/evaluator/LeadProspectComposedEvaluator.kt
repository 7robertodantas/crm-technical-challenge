package com.addi.business.evaluator

import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.outcome.LeadEvaluationOutcome
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LeadProspectComposedEvaluator(
    private val nationalRegistryEvaluator: NationalRegistryEvaluator,
    private val judicialRecordsEvaluator: JudicialRecordsEvaluator,
    private val scoreQualificationEvaluator: ScoreQualificationEvaluator
) : LeadEvaluator {
    override suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome = coroutineScope {
        listOf(nationalRegistryEvaluator, judicialRecordsEvaluator)

            // evaluate national registry and judicial records in parallel and wait them
            .map { evaluator -> async { evaluator.evaluate(command) } }
            .awaitAll()

            // combine those results - pick the first error that may occur or success if both were succeeded
            .fold(initial = LeadEvaluationOutcome.success(), operation = LeadEvaluationOutcome::combine)

            // if the previous is succeeded then get result of score qualification evaluation
            .flatMap { scoreQualificationEvaluator.evaluate(command) }
    }
}