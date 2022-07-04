package com.addi.application

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.ProspectQualification
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.JsonBody
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.ExperimentalTime


@ExperimentalTime
@ExtendWith(value = [MockServerExtension::class])
internal class LeadEvaluationIntegrationTest(val mockServer: ClientAndServer) {

    private val mockServerUrl = "http://localhost:${mockServer.port}"
    private val objectMapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .build()

    private val app = LeadEvaluationApplication(
        nationalRegistryUrl = mockServerUrl,
        judicialArchiveUrl = mockServerUrl,
        prospectQualifierUrl = mockServerUrl,
    )

    @Test
    fun `it should execute whole flow`(): Unit = runBlocking {
        val nationalIdNumber = "54a7f225-13fa-47e1-b3ce-2315b1d4785b"
        val personRegistry = PersonRegistry(
            nationalIdNumber = nationalIdNumber,
            birthDate = LocalDate.now(),
            firstName = "foo",
            lastName = "bar",
            email = "a91d9614-190f-4a84-9aad-2c699d64d838@email.com"
        )
        val judicialRecord = JudicialRecord(
            nationalIdNumber = nationalIdNumber,
            hasRecords = false
        )
        val score = ProspectQualification(
            score = 65
        )

        mockServer.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/registry")
        ).respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(personRegistry)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 300).toLong())
                )
        )

        mockServer.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/judicial")
        ).respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(judicialRecord)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 200).toLong())
                )
        )

        mockServer.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/score")
        ).respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(score)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 200).toLong())
                )
        )

        val result = app.evaluate(nationalIdNumber)
        assertThat(result.isSuccess()).isTrue
    }

}