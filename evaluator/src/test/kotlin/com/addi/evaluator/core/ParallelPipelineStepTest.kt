package com.addi.evaluator.core

import com.addi.evaluator.domain.EvaluationOutcome
import com.addi.evaluator.domain.PipelineParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class ParallelPipelineStepTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val parameterAValue = "64c277c3"
    private val leadEvaluateCommand = PipelineParameters(mapOf(
        TestBucket.TEST to parameterAValue
    ))

    @Test
    fun `it should execute all in parallel and combine results even if one fails`() = testScope.runBlockingTest {

        val leadEvaluatorA = mockkClass(EvaluatorStep::class)
        val leadEvaluatorB = mockkClass(EvaluatorStep::class)

        coEvery { leadEvaluatorA.evaluate(eq(leadEvaluateCommand)) }
            .coAnswers {
                return@coAnswers EvaluationOutcome.fail("something went wrong with A")
            }

        coEvery { leadEvaluatorB.evaluate(eq(leadEvaluateCommand)) }
            .coAnswers {
                return@coAnswers EvaluationOutcome.success()
            }

        val evaluator = ParallelPipelineStep(
            testDispatcher,
            leadEvaluatorA,
            leadEvaluatorB
        )

        val result = measureTimedValue {
            evaluator.evaluate(leadEvaluateCommand)
        }

        coVerify(exactly = 1) { leadEvaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { leadEvaluatorB.evaluate(eq(leadEvaluateCommand)) }

        assertThat(result.value.success).isFalse
        assertThat(result.value.isFail()).isTrue
        assertThat(result.value.isSuccess()).isFalse
        assertThat(result.value).isEqualTo(EvaluationOutcome.fail("something went wrong with A"))
    }

    @Test
    fun `it should execute all in parallel and combine results all succeed`() = testScope.runBlockingTest {

        val leadEvaluatorA = mockkClass(EvaluatorStep::class)
        val leadEvaluatorB = mockkClass(EvaluatorStep::class)

        coEvery { leadEvaluatorA.evaluate(eq(leadEvaluateCommand)) }
            .coAnswers {
                return@coAnswers EvaluationOutcome.success()
            }

        coEvery { leadEvaluatorB.evaluate(eq(leadEvaluateCommand)) }
            .coAnswers {
                return@coAnswers EvaluationOutcome.success()
            }

        val evaluator = ParallelPipelineStep(
            testDispatcher,
            leadEvaluatorA,
            leadEvaluatorB
        )

        val result = measureTimedValue {
            evaluator.evaluate(leadEvaluateCommand)
        }

        coVerify(exactly = 1) { leadEvaluatorA.evaluate(eq(leadEvaluateCommand)) }
        coVerify(exactly = 1) { leadEvaluatorB.evaluate(eq(leadEvaluateCommand)) }

        assertThat(result.value.success).isTrue
        assertThat(result.value.isFail()).isFalse
        assertThat(result.value.isSuccess()).isTrue
        assertThat(result.value).isEqualTo(EvaluationOutcome.success())
    }


}