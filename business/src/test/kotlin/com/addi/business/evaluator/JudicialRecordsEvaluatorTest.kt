package com.addi.business.evaluator

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import com.addi.business.thirdparty.dto.JudicialRecord
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JudicialRecordsEvaluatorTest {

    private val nationalNumber = "8e9bf29a"
    private val getPersonDataCommand = GetPersonDataCommand(nationalNumber)
    private val leadEvaluateCommand = LeadEvaluateCommand(nationalNumber)

    private val judicialRecordArchive = mockkClass(JudicialRecordArchive::class)
    private val evaluator = JudicialRecordsEvaluator(judicialRecordArchive)

    @Test
    fun `it should fail if has judicial records`() {
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(
            nationalIdNumber = nationalNumber,
            hasRecords = true
        )

        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }

        assertThat(result.converted).isFalse
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

        assertThat(result.converted).isTrue
        assertThat(result.isFail()).isFalse
        assertThat(result.isSuccess()).isTrue
        assertThat(result).isEqualTo(EvaluationOutcome.success())
    }


}