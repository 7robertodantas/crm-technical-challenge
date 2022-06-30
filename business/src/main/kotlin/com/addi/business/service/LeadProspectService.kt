package com.addi.business.service

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.command.GetProspectQualificationCommand
import com.addi.business.command.LeadProspectCommand
import com.addi.business.database.PersonRepository
import com.addi.business.domain.Person
import com.addi.business.outcome.LeadEvaluationOutcome
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.ProspectQualifier
import com.addi.business.thirdparty.dto.PersonRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LeadProspectService(
    private val nationalRegistry: NationalRegistry,
    private val judicialRecordArchive: JudicialRecordArchive,
    private val prospectQualifier: ProspectQualifier,
    private val personRepository: PersonRepository
) {

    suspend fun handle(command: LeadProspectCommand): LeadEvaluationOutcome = coroutineScope {
        val getPersonDataCommand = toPersonDataCommand(command)

        val deferredRegistry = async {
            nationalRegistry.getRegistry(getPersonDataCommand)
        }

        val deferredJudicial = async {
            judicialRecordArchive.getRegistry(getPersonDataCommand)
        }

        val registry = deferredRegistry.await()

        val judicial = deferredJudicial.await()

        if (judicial.hasRecords) {
            return@coroutineScope LeadEvaluationOutcome.fail("person has judicial records")
        }

        val person = toPersonDomain(registry)
        if (!personRepository.matchStored(person)) {
            return@coroutineScope LeadEvaluationOutcome.fail("personal information does not match")
        }

        val qualifyCommand = toGetProspectQualificationCommand(command)
        if (prospectQualifier.getScore(qualifyCommand).score <= 60) {
            return@coroutineScope LeadEvaluationOutcome.fail("lead score is below 60");
        }

        return@coroutineScope LeadEvaluationOutcome.success()

    }

    private fun toPersonDataCommand(command: LeadProspectCommand): GetPersonDataCommand = GetPersonDataCommand(
        nationalIdNumber = command.nationalIdNumber
    )

    private fun toGetProspectQualificationCommand(command: LeadProspectCommand): GetProspectQualificationCommand = GetProspectQualificationCommand(
        nationalIdNumber = command.nationalIdNumber
    )

    private fun toPersonDomain(registry: PersonRegistry): Person = Person(
        nationalIdNumber = registry.nationalIdNumber,
        birthDate = registry.birthDate,
        firstName = registry.firstName,
        lastName = registry.lastName,
        email = registry.email
    )

}