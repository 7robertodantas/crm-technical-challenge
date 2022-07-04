package com.addi.business.evaluator

import com.addi.business.domain.Person
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.evaluator.core.PipelineParameters
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.evaluator.core.EvaluationBucket
import com.addi.business.evaluator.core.EvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.evaluator.core.EvaluatorStep
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.PersonRepository

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
                    EvaluationBucket.PERSON_EXISTS to "true",
                    EvaluationBucket.PERSON_MATCHES_INTERNAL to "true"
                )
            )

        } catch (ex: PersonNotFoundException) {
            return EvaluationOutcome.fail("person does not exist on national registry identification")
        } catch (ex: Exception) {
            return EvaluationOutcome.fail(ex)
        }
    }

}