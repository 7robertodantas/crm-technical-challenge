package com.addi.application.stub

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

    /**
     * Starts a new embedded mockserver in a random port.
     */
    fun start() {
        log.info("Starting embedded mockserver")
        mockServer = ClientAndServer.startClientAndServer(
            Configuration.configuration()
                .logLevel("WARN")
        )
    }

    /**
     * Returns the url of mockserver.
     */
    fun getUrl(): String {
        return "http://localhost:${mockServer?.port}"
    }

    /**
     * Stubs all requests used by the evaluation process.
     * Using random delay between 100ms to 300ms
     */
    fun stub(nationalIdNumber: String) {
        stub(
            path = "/persons/$nationalIdNumber/registry",
            delay = Random.nextInt(100, 300).toLong(),
            response = PersonRegistry(
                nationalIdNumber = nationalIdNumber,
                birthDate = LocalDate.now(),
                firstName = "foo",
                lastName = "bar",
                email = "$nationalIdNumber@email.com"
            )
        )

        stub(
            path = "/persons/$nationalIdNumber/judicial",
            delay = Random.nextInt(100, 300).toLong(),
            response = JudicialRecord(
                nationalIdNumber = nationalIdNumber,
                hasRecords = false
            )
        )

        stub(
            path = "/persons/$nationalIdNumber/score",
            delay = Random.nextInt(100, 300).toLong(),
            response = ProspectQualification(
                score = 65
            )
        )
    }

    private fun stub(path: String, delay: Long, response: Any) {
        log.info("Stubbing request '$path' with delay of '$delay'ms")
        mockServer?.`when`(
            HttpRequest.request(path)
        )?.respond(
            HttpResponse.response()
                .withBody(JsonBody(objectMapper.writeValueAsString(response)))
                .withStatusCode(200)
                .withDelay(
                    Delay.milliseconds(delay)
                )
        )
    }


    /**
     * Stop embedded mockserver.
     */
    fun stop() {
        log.info("Stopping embedded mockserver")
        mockServer?.stop()
    }


}