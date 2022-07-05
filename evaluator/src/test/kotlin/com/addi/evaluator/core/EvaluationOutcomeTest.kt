package com.addi.evaluator.core

import com.addi.evaluator.domain.EvaluationOutcome
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EvaluationOutcomeTest {

    @Test
    fun `combine should return current if failed`() {
        val failed = EvaluationOutcome.fail("error")
        val succeeded = EvaluationOutcome.success()

        assertThat(failed.combine(succeeded)).isEqualTo(failed)
    }

    @Test
    fun `combine should return next error if current is succeed`() {
        val succeeded = EvaluationOutcome.success()
        val failed = EvaluationOutcome.fail("error")

        assertThat(succeeded.combine(failed)).isEqualTo(failed)
    }

    @Test
    fun `combine should return next success if current is succeed`() {
        val succeeded = EvaluationOutcome.success()
        val otherSucceeded = EvaluationOutcome.success(mapOf(
            TestBucket.TEST to "true"
        ))

        assertThat(succeeded.combine(otherSucceeded)).isEqualTo(otherSucceeded)
    }

    @Test
    fun `flatmap should not apply function if current is failed`() {
        val failed = EvaluationOutcome.fail("error")
        val otherSucceeded = mockkClass(EvaluationOutcome::class)

        val result = runBlocking { failed.flatMap { otherSucceeded } }
        assertThat(result).isEqualTo(failed)
    }

    @Test
    fun `flatmap should apply function if current is succeed`() {
        val succeeded = EvaluationOutcome.success(mapOf(
            TestBucket.TEST to "01ed114c-76c8-445d-8f8e-8b4c750acea7"
        ))
        val otherSucceeded = EvaluationOutcome.success(mapOf(
            TestBucket.B to "true"
        ))

        val result = runBlocking { succeeded.flatMap { otherSucceeded } }
        assertThat(result).isEqualTo(otherSucceeded.combine(succeeded))
    }

    @Test
    fun `is fail should return true if is not converted and has error`() {
        val failed = EvaluationOutcome.fail("error")
        assertThat(failed.isFail()).isTrue
    }

    @Test
    fun `is success should return true if converted and has no error`() {
        val succeeded = EvaluationOutcome.success()
        assertThat(succeeded.isSuccess()).isTrue
    }

}