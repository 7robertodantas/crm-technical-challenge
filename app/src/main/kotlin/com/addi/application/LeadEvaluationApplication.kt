package com.addi.application

import com.addi.application.config.ApplicationConfiguration
import com.addi.application.factory.LeadProspectServiceFactory
import com.addi.application.stub.EmbeddedMockserverStub
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
class LeadEvaluationApplication(val config: ApplicationConfiguration) {

    private val service: LeadProspectService = LeadProspectServiceFactory.createService(
        config.nationalRegistryUrl,
        config.judicialArchiveUrl,
        config.prospectQualifierUrl
    )

    suspend fun evaluate(nationalIdNumber: String): EvaluationOutcome {
        if (config.embeddedMockServer) {
            EmbeddedMockserverStub.stub(nationalIdNumber)
        }

        log.info("Processing national id number '$nationalIdNumber'")
        val outcome = service.evaluate(
            PipelineParameters(
                mapOf(
                    LeadEvaluationBucket.NATIONAL_ID_NUMBER to nationalIdNumber
                )
            )
        )
        log.info("Result is ${objectMapper.writeValueAsString(outcome)}")
        return outcome
    }

    companion object {
        private val log = LoggerFactory.getLogger(LeadEvaluationApplication::class.java)
        private val objectMapper = jacksonMapperBuilder()
            .addModule(JavaTimeModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build()

        @JvmStatic
        fun main(args: Array<String>) {
            val app = LeadEvaluationApplication(config = ApplicationConfiguration.load())
            val nationalIdNumber = args.firstOrNull() ?: UUID.randomUUID().toString()
            runBlocking {
                app.evaluate(nationalIdNumber)
            }
        }
    }

}

