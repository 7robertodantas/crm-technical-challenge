package com.addi.business.evaluator

import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.domain.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.ProspectQualifier
import com.addi.business.domain.ProspectQualification
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ScoreQualificationEvaluatorTest {

    private val nationalIdNumber = "c02f4e2c"
    private val leadEvaluateCommand = LeadEvaluateCommand(nationalIdNumber)
    private val getScoreCommand = GetProspectQualificationCommand(nationalIdNumber)

    private val prospectQualifier = mockkClass(ProspectQualifier::class)
    private val minimumScore = 60
    private val evaluator = ScoreQualificationEvaluator(
        prospectQualifier, minimumScore
    )

    @Test
    fun `it should fail if some exception is thrown`() {
        val exception = Exception("oh no!")
        coEvery { prospectQualifier.getScore(eq(getScoreCommand)) } throws exception
        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.converted).isFalse
        assertThat(result.isFail()).isTrue
        assertThat(result.isSuccess()).isFalse
        assertThat(result).isEqualTo(
            EvaluationOutcome.fail(exception)
        )
    }

    @Test
    fun `it should fail if score is below minimum`() {
        (0..60).forEach { score ->
            coEvery { prospectQualifier.getScore(eq(getScoreCommand)) } returns ProspectQualification(
                score
            )
            val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
            assertThat(result.converted).isFalse
            assertThat(result.isFail()).isTrue
            assertThat(result.isSuccess()).isFalse
            assertThat(result).isEqualTo(
                EvaluationOutcome.fail("lead score '$score' returned is below minimum score of '$minimumScore'")
            )
        }
    }

    @Test
    fun `it should succeed if score is equal or above minimum`() {
        (61..100).forEach { score ->
            coEvery { prospectQualifier.getScore(eq(getScoreCommand)) } returns ProspectQualification(
                score
            )
            val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
            assertThat(result.converted).isTrue
            assertThat(result.isFail()).isFalse
            assertThat(result.isSuccess()).isTrue
            assertThat(result).isEqualTo(
                EvaluationOutcome.success()
            )
        }
    }

}