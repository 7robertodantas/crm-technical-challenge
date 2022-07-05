package com.addi.application

import com.addi.business.adapter.JudicialRecordArchive
import com.addi.business.adapter.NationalRegistry
import com.addi.business.adapter.PersonRepository
import com.addi.business.adapter.ProspectQualifier
import com.addi.business.domain.Person
import com.addi.business.domain.evaluator.LeadEvaluationBucket
import com.addi.business.service.LeadProspectService
import com.addi.business.service.LeadProspectServiceImpl
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters
import com.addi.thirdparty.JudicialRecordArchiveClient
import com.addi.thirdparty.NationalRegistryClient
import com.addi.thirdparty.ProspectQualifierClient
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LeadEvaluationApplication(
    private val nationalRegistryUrl: String,
    private val judicialArchiveUrl: String,
    private val prospectQualifierUrl: String
) {

    private val nationalRegistry: NationalRegistry = NationalRegistryClient(nationalRegistryUrl)
    private val judicialRecordArchive: JudicialRecordArchive = JudicialRecordArchiveClient(judicialArchiveUrl)
    private val prospectQualifier: ProspectQualifier = ProspectQualifierClient(prospectQualifierUrl)
    private val personRepository = object : PersonRepository {
        override suspend fun matchStored(person: Person): Boolean {
            return true;
        }
    }
    private val service: LeadProspectService = LeadProspectServiceImpl(
        nationalRegistry,
        personRepository,
        judicialRecordArchive,
        prospectQualifier
    )

    suspend fun evaluate(nationalIdNumber: String): EvaluationOutcome {
        return service.evaluate(
            PipelineParameters(
                mapOf(
                    LeadEvaluationBucket.NATIONAL_ID_NUMBER to nationalIdNumber
                )
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LeadEvaluationApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val app = LeadEvaluationApplication(
                nationalRegistryUrl = System.getenv("NATIONAL_REGISTRY_URL")
                    ?: "http://localhost:8080/national-registry",
                judicialArchiveUrl = System.getenv("JUDICIAL_ARCHIVE_URL")
                    ?: "http://localhost:8080/judicial-archive",
                prospectQualifierUrl = System.getenv("PROSPECT_QUALIFIER_URL")
                    ?: "http://localhost:8080/prospect-qualifier"
            )

            runBlocking {
                val result = app.evaluate(args.firstOrNull() ?: UUID.randomUUID().toString())
                logger.info("Result is $result")
            }
        }
    }

}

