package com.addi.application

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.ProspectQualification
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.mockserver.configuration.Configuration
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.JsonBody
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.random.Random

object EmbeddedMockserverStub {

    private val log = LoggerFactory.getLogger(EmbeddedMockserverStub::class.java)

    private val objectMapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .build()

    private var mockServer: ClientAndServer? = null

    fun start() {
        log.info("Starting embedded mockserver")
        mockServer = ClientAndServer.startClientAndServer(
            Configuration.configuration()
                .logLevel("WARN")
        )
    }

    fun getUrl(): String {
        return "http://localhost:${mockServer?.port}"
    }

    fun stub(nationalIdNumber: String) {
        val personRegistry = PersonRegistry(
            nationalIdNumber = nationalIdNumber,
            birthDate = LocalDate.now(),
            firstName = "foo",
            lastName = "bar",
            email = "$nationalIdNumber@email.com"
        )
        val judicialRecord = JudicialRecord(
            nationalIdNumber = nationalIdNumber,
            hasRecords = false
        )
        val score = ProspectQualification(
            score = 65
        )

        mockServer?.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/registry")
        )?.respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(personRegistry)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 300).toLong())
                )
        )

        mockServer?.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/judicial")
        )?.respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(judicialRecord)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 200).toLong())
                )
        )

        mockServer?.`when`(
            HttpRequest.request("/persons/$nationalIdNumber/score")
        )?.respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(score)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(Random.nextInt(100, 200).toLong())
                )
        )
    }

    fun stop() {
        log.info("Stopping embedded mockserver")
        mockServer?.stop()
    }


}