package com.addi.business.steps

import com.addi.business.domain.Person
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.domain.evaluator.LeadEvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.domain.evaluator.LeadEvaluationBucket.PERSON_EXISTS
import com.addi.business.domain.evaluator.LeadEvaluationBucket.PERSON_MATCHES_INTERNAL
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.core.EvaluatorStep
import com.addi.evaluator.domain.PipelineParameters
import com.addi.business.adapter.NationalRegistry
import com.addi.business.adapter.PersonRepository

/**
 * This performs the following evaluation:
 *
 * The person should exist in the national registry identification external system
 * and their personal information should match the information stored in our
 * local database.
 */
class NationalRegistryEvaluatorStep(
    private val nationalRegistry: NationalRegistry,
    private val personRepository: PersonRepository
) : EvaluatorStep {

    override suspend fun evaluate(parameters: PipelineParameters): EvaluationOutcome {
        try {
            val registry = nationalRegistry.getRegistry(GetPersonDataCommand(parameters.get(NATIONAL_ID_NUMBER)))

            val person = Person(
                nationalIdNumber = registry.nationalIdNumber,
                birthDate = registry.birthDate,
                firstName = registry.firstName,
                lastName = registry.lastName,
                email = registry.email
            )
            if (!personRepository.matchStored(person)) {
                return EvaluationOutcome.fail("personal information does not match")
            }

            return EvaluationOutcome.success(
                mapOf(
                    PERSON_EXISTS to "true",
                    PERSON_MATCHES_INTERNAL to "true"
                )
            )

        } catch (ex: PersonNotFoundException) {
            return EvaluationOutcome.fail("person does not exist on national registry identification")
        } catch (ex: Exception) {
            return EvaluationOutcome.fail(ex)
        }
    }

}