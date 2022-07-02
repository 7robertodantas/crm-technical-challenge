package com.addi.business.evaluator.core

import com.addi.business.command.LeadEvaluateCommand
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class SequentialEvaluatorTest {

    private val nationalNumber = "4a5230dc"
    private val leadEvaluateCommand = LeadEvaluateCommand(nationalNumber)

    @Test
    fun `it should evaluate all in sequence`() {
        val evaluatorA = mockkClass(LeadEvaluator::class)
        val evaluatorB = mockkClass(LeadEvaluator::class)
        val evaluatorC = mockkClass(LeadEvaluator::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()

        val sequentialEvaluator = SequentialEvaluator(
           evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.converted).isTrue
        Assertions.assertThat(result.isFail()).isFalse
        Assertions.assertThat(result.isSuccess()).isTrue
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.success())

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

    @Test
    fun `it should fail first and not execute following`() {
        val evaluatorA = mockkClass(LeadEvaluator::class)
        val evaluatorB = mockkClass(LeadEvaluator::class)
        val evaluatorC = mockkClass(LeadEvaluator::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.fail("Something went wrong with B")
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()

        val sequentialEvaluator = SequentialEvaluator(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.converted).isFalse
        Assertions.assertThat(result.isFail()).isTrue
        Assertions.assertThat(result.isSuccess()).isFalse
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.fail("Something went wrong with B"))

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 0) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

    @Test
    fun `it should fail if last fails`() {
        val evaluatorA = mockkClass(LeadEvaluator::class)
        val evaluatorB = mockkClass(LeadEvaluator::class)
        val evaluatorC = mockkClass(LeadEvaluator::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.fail("Something went wrong with C")

        val sequentialEvaluator = SequentialEvaluator(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.converted).isFalse
        Assertions.assertThat(result.isFail()).isTrue
        Assertions.assertThat(result.isSuccess()).isFalse
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.fail("Something went wrong with C"))

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

}