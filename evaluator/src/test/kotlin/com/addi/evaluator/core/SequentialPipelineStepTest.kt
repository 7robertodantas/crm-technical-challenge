package com.addi.evaluator.core

import com.addi.evaluator.domain.EvaluationBucket
import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class SequentialPipelineStepTest {

    private val parameterAValue = "4a5230dc"
    private val initialBucket: Map<EvaluationBucket, String> = mapOf(
        TestBucket.TEST to parameterAValue
    )
    private val leadEvaluateCommand = PipelineParameters(initialBucket)

    @Test
    fun `it should accumulate parameters`() {
        val evaluatorA = mockkClass(EvaluatorStep::class)
        val evaluatorB = mockkClass(EvaluatorStep::class)
        val evaluatorC = mockkClass(EvaluatorStep::class)

        val outcomeA = mapOf<EvaluationBucket, String>(
            TestBucket.A to "true"
        )

        val outcomeB = mapOf<EvaluationBucket, String>(
            TestBucket.B to "true"
        )

        val outcomeC = mapOf<EvaluationBucket, String>(
            TestBucket.C to "true"
        )
        val leadEvaluateCommandA = PipelineParameters(initialBucket)
        val leadEvaluateCommandB = PipelineParameters(initialBucket + outcomeA)
        val leadEvaluateCommandC = PipelineParameters(initialBucket + outcomeA + outcomeB)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommandA)) } returns EvaluationOutcome.success(outcomeA)
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommandB)) } returns EvaluationOutcome.success(outcomeB)
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommandC)) } returns EvaluationOutcome.success(outcomeC)

        val sequentialEvaluator = SequentialPipelineStep(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.success).isTrue
        Assertions.assertThat(result.isFail()).isFalse
        Assertions.assertThat(result.isSuccess()).isTrue
        Assertions.assertThat(result).isEqualTo(
            EvaluationOutcome.success(
                initialBucket + outcomeA + outcomeB + outcomeC
            )
        )

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommandA)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommandB)) }
        coVerify(exactly = 1) { evaluatorC.evaluate(eq(leadEvaluateCommandC)) }
    }

    @Test
    fun `it should evaluate all in sequence`() {
        val evaluatorA = mockkClass(EvaluatorStep::class)
        val evaluatorB = mockkClass(EvaluatorStep::class)
        val evaluatorC = mockkClass(EvaluatorStep::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()

        val sequentialEvaluator = SequentialPipelineStep(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.success).isTrue
        Assertions.assertThat(result.isFail()).isFalse
        Assertions.assertThat(result.isSuccess()).isTrue
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.success(initialBucket))

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

    @Test
    fun `it should fail first and not execute following`() {
        val evaluatorA = mockkClass(EvaluatorStep::class)
        val evaluatorB = mockkClass(EvaluatorStep::class)
        val evaluatorC = mockkClass(EvaluatorStep::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.fail("Something went wrong with B")
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()

        val sequentialEvaluator = SequentialPipelineStep(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.success).isFalse
        Assertions.assertThat(result.isFail()).isTrue
        Assertions.assertThat(result.isSuccess()).isFalse
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.fail("Something went wrong with B", initialBucket))

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 0) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

    @Test
    fun `it should fail if last fails`() {
        val evaluatorA = mockkClass(EvaluatorStep::class)
        val evaluatorB = mockkClass(EvaluatorStep::class)
        val evaluatorC = mockkClass(EvaluatorStep::class)

        coEvery { evaluatorA.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorB.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.success()
        coEvery { evaluatorC.evaluate(eq(leadEvaluateCommand)) } returns EvaluationOutcome.fail(
            "Something went wrong with C",
            initialBucket
        )

        val sequentialEvaluator = SequentialPipelineStep(
            evaluatorA, evaluatorB, evaluatorC
        )

        val result = runBlocking { sequentialEvaluator.evaluate(leadEvaluateCommand) }
        Assertions.assertThat(result.success).isFalse
        Assertions.assertThat(result.isFail()).isTrue
        Assertions.assertThat(result.isSuccess()).isFalse
        Assertions.assertThat(result).isEqualTo(EvaluationOutcome.fail("Something went wrong with C", initialBucket))

        coVerify(exactly = 1) { evaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorB.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { evaluatorC.evaluate(eq(leadEvaluateCommand)) }
    }

}