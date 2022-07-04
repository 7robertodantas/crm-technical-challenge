package com.addi.business.evaluator

import com.addi.business.domain.Person
import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.evaluator.LeadEvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.evaluator.LeadEvaluationBucket.PERSON_EXISTS
import com.addi.business.evaluator.LeadEvaluationBucket.PERSON_MATCHES_INTERNAL
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.evaluator.core.PipelineParameters
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.PersonRepository
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class NationalRegistryEvaluatorTest {

    private val nationalNumber = "91c39365"
    private val getPersonDataCommand = GetPersonDataCommand(nationalNumber)
    private val leadEvaluateCommand = PipelineParameters(mapOf(
        NATIONAL_ID_NUMBER to nationalNumber
    ))

    private val nationalRegistry = mockkClass(NationalRegistry::class)
    private val personRepository = mockkClass(PersonRepository::class)
    private val evaluator = NationalRegistryEvaluatorStep(
        nationalRegistry, personRepository
    )

    private val birthDate = LocalDate.of(2022, 1, 1)
    private val firstName = "foo"
    private val lastName = "bar"
    private val email = "foo@email.com"

    @Test
    fun `it should fail if person does not exist`() {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } throws PersonNotFoundException("could not find person")

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail("person does not exist on national registry identification"))
    }

    @Test
    fun `it should fail if some exception is thrown`() {
        val exception = Exception("could not connect")
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } throws exception

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail(exception))
    }

    @Test
    fun `it should fail if person data does not match`() {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns PersonRegistry(
            nationalNumber,
            birthDate,
            firstName,
            lastName,
            email
        )

        coEvery { personRepository.matchStored(eq(Person(
            nationalNumber,
            birthDate,
            firstName,
            lastName,
            email
        ))) } returns false

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail("personal information does not match"))
    }

    @Test
    fun `it should succeed if person data matches`() {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns PersonRegistry(
            nationalNumber,
            birthDate,
            firstName,
            lastName,
            email
        )

        coEvery { personRepository.matchStored(eq(Person(
            nationalNumber,
            birthDate,
            firstName,
            lastName,
            email
        ))) } returns true

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.success).isTrue
        assertThat(result.isFail()).isFalse
        assertThat(result.isSuccess()).isTrue
        assertThat(result).isEqualTo(EvaluationOutcome.success(
            mapOf(
                PERSON_EXISTS to "true",
                PERSON_MATCHES_INTERNAL to "true"
            )
        ))
    }

}