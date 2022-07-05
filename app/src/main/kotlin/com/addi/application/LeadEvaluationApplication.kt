package com.addi.application

import com.addi.business.domain.evaluator.LeadEvaluationBucket
import com.addi.business.service.LeadProspectService
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LeadEvaluationApplication(config: ApplicationConfiguration) {

    private val service: LeadProspectService = LeadProspectServiceFactory.createService(
        config.nationalRegistryUrl,
        config.judicialArchiveUrl,
        config.prospectQualifierUrl
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
        private val log = LoggerFactory.getLogger(LeadEvaluationApplication::class.java)
        private const val EMBEDDED_MOCKSERVER_ENV = "EMBEDDED_MOCKSERVER_STUB"
        private const val NATIONAL_REGISTRY_URL_ENV = "NATIONAL_REGISTRY_URL"
        private const val JUDICIAL_ARCHIVE_URL_ENV = "JUDICIAL_ARCHIVE_URL"
        private const val PROSPECT_QUALIFIER_URL_ENV = "PROSPECT_QUALIFIER_URL"
        private val objectMapper = jacksonMapperBuilder()
            .addModule(JavaTimeModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build()

        @JvmStatic
        fun main(args: Array<String>) {
            val nationalIdNumber = args.firstOrNull() ?: UUID.randomUUID().toString()
            log.info("Using national id number as '$nationalIdNumber'")

            val embeddedMockserver = (System.getenv(EMBEDDED_MOCKSERVER_ENV) ?: "true").toBoolean()
            val config = if (embeddedMockserver) {
                log.info("Environment variable $EMBEDDED_MOCKSERVER_ENV is enabled.")
                EmbeddedMockserverStub.start()
                Runtime.getRuntime().addShutdownHook(Thread {
                    EmbeddedMockserverStub.stop()
                })
                log.info("Stubbing http endpoints")
                EmbeddedMockserverStub.stub(nationalIdNumber)
                ApplicationConfiguration(
                    nationalRegistryUrl = EmbeddedMockserverStub.getUrl(),
                    judicialArchiveUrl = EmbeddedMockserverStub.getUrl(),
                    prospectQualifierUrl = EmbeddedMockserverStub.getUrl(),
                )
            } else {
                ApplicationConfiguration(
                    nationalRegistryUrl = System.getenv(NATIONAL_REGISTRY_URL_ENV)
                        ?: "http://localhost:8080/national-registry",
                    judicialArchiveUrl = System.getenv(JUDICIAL_ARCHIVE_URL_ENV)
                        ?: "http://localhost:8080/judicial-archive",
                    prospectQualifierUrl = System.getenv(PROSPECT_QUALIFIER_URL_ENV)
                        ?: "http://localhost:8080/prospect-qualifier",
                )
            }

            val app = LeadEvaluationApplication(config)

            runBlocking {
                val result = app.evaluate(nationalIdNumber)
                log.info("Result is ${objectMapper.writeValueAsString(result)}")
            }
        }
    }

}

