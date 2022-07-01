package com.addi.business.evaluator

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.database.PersonRepository
import com.addi.business.domain.Person
import com.addi.business.outcome.LeadEvaluationOutcome
import com.addi.business.thirdparty.adapter.NationalRegistry

class NationalRegistryEvaluator(
    private val nationalRegistry: NationalRegistry,
    private val personRepository: PersonRepository
) : LeadEvaluator {

    override suspend fun evaluate(command: LeadEvaluateCommand): LeadEvaluationOutcome {
        val registry = nationalRegistry.getRegistry(GetPersonDataCommand(command.nationalIdNumber))
        val person = Person(
            nationalIdNumber = registry.nationalIdNumber,
            birthDate = registry.birthDate,
            firstName = registry.firstName,
            lastName = registry.lastName,
            email = registry.email
        )
        if (!personRepository.matchStored(person)) {
            return LeadEvaluationOutcome.fail("personal information does not match")
        }

        return LeadEvaluationOutcome.success()
    }

}