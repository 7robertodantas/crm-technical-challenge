package com.addi.business.steps

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.domain.evaluator.LeadEvaluationBucket.NATIONAL_ID_NUMBER
import com.addi.business.domain.evaluator.LeadEvaluationBucket.PERSON_HAS_JUDICIAL_RECORDS
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters
import com.addi.business.adapter.JudicialRecordArchive
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JudicialRecordsEvaluatorTest {

    private val nationalNumber = "8e9bf29a"
    private val getPersonDataCommand = GetPersonDataCommand(nationalNumber)
    private val leadEvaluateCommand = PipelineParameters(mapOf(
        NATIONAL_ID_NUMBER to nationalNumber
    ))

    private val judicialRecordArchive = mockkClass(JudicialRecordArchive::class)
    private val evaluator = JudicialRecordsEvaluatorStep(judicialRecordArchive)


    @Test
    fun `it should fail if some exception is thrown`() {
        val exception = Exception("could not connect")
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } throws exception

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }

        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail(exception))
    }

    @Test
    fun `it should fail if could not get judicial records`() {
        val exception = PersonNotFoundException("could not connect")
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } throws exception

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }

        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail("failed to fetch person judicial records"))
    }

    @Test
    fun `it should fail if has judicial records`() {
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(
            nationalIdNumber = nationalNumber,
            hasRecords = true
        )

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }

        assertThat(result.success).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(EvaluationOutcome.fail("person has judicial records"))
    }

    @Test
    fun `it should succeed if has no judicial records`() {
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(
            nationalIdNumber = nationalNumber,
            hasRecords = false
        )

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }

        assertThat(result.success).isTrue
        assertThat(result.isFail()).isFalse
        assertThat(result.isSuccess()).isTrue
        assertThat(result).isEqualTo(
            EvaluationOutcome.success(
            mapOf(
                PERSON_HAS_JUDICIAL_RECORDS to "true"
            )
        ))
    }



}