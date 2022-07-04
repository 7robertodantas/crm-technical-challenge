package com.addi.business.evaluator

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.thirdparty.adapter.PersonRepository
import com.addi.business.domain.Person
import com.addi.business.evaluator.core.LeadEvaluator
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.NationalRegistry

class NationalRegistryEvaluator(
    private val nationalRegistry: NationalRegistry,
    private val personRepository: PersonRepository
) : LeadEvaluator {

    override suspend fun evaluate(command: LeadEvaluateCommand): EvaluationOutcome {
        val registry = nationalRegistry.getRegistry(GetPersonDataCommand(command.nationalIdNumber))
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

        return EvaluationOutcome.success()
    }

}