package com.addi.application.factory

import com.addi.business.adapter.JudicialRecordArchive
import com.addi.business.adapter.NationalRegistry
import com.addi.business.adapter.PersonRepository
import com.addi.business.adapter.ProspectQualifier
import com.addi.business.domain.Person
import com.addi.business.service.LeadProspectService
import com.addi.business.service.LeadProspectServiceImpl
import com.addi.thirdparty.JudicialRecordArchiveClient
import com.addi.thirdparty.NationalRegistryClient
import com.addi.thirdparty.ProspectQualifierClient
import kotlin.time.ExperimentalTime

@ExperimentalTime
object LeadProspectServiceFactory {

    /**
     * Factory method to create lead prospect service with all dependencies.
     */
    fun createService(nationalRegistryUrl: String,
                      judicialArchiveUrl: String,
                      prospectQualifierUrl: String): LeadProspectService {
        val nationalRegistry: NationalRegistry = NationalRegistryClient(nationalRegistryUrl)
        val judicialRecordArchive: JudicialRecordArchive = JudicialRecordArchiveClient(judicialArchiveUrl)
        val prospectQualifier: ProspectQualifier = ProspectQualifierClient(prospectQualifierUrl)
        val personRepository = object : PersonRepository {
            override suspend fun matchStored(person: Person): Boolean {
                return true;
            }
        }
        return LeadProspectServiceImpl(
            nationalRegistry,
            personRepository,
            judicialRecordArchive,
            prospectQualifier
        )
    }

}