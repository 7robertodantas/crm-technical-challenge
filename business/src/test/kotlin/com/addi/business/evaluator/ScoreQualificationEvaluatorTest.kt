package com.addi.business.evaluator

import com.addi.business.command.GetProspectQualificationCommand
import com.addi.business.command.LeadEvaluateCommand
import com.addi.business.evaluator.core.EvaluationOutcome
import com.addi.business.thirdparty.adapter.ProspectQualifier
import com.addi.business.thirdparty.dto.ProspectQualification
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ScoreQualificationEvaluatorTest {

    private val nationalIdNumber = "c02f4e2c"
    private val leadEvaluateCommand = LeadEvaluateCommand(nationalIdNumber)
    private val getScoreCommand = GetProspectQualificationCommand(nationalIdNumber)

    @Test
    fun `it should fail if score is below minimum`() {
        val prospectQualifier = mockkClass(ProspectQualifier::class)
        val minimumScore = 60
        val evaluator = ScoreQualificationEvaluator(
            prospectQualifier, minimumScore
        )

        (0..60).forEach { score ->
            coEvery { prospectQualifier.getScore(eq(getScoreCommand)) } returns ProspectQualification(
                score
            )
            val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
            assertThat(result.converted).isFalse
            assertThat(result.isFail()).isTrue
            assertThat(result.isSuccess()).isFalse
            assertThat(result).isEqualTo(
                EvaluationOutcome.fail("lead score is below minimum score of '$minimumScore'")
            )
        }
    }

    @Test
    fun `it should succeed if score is equal or above minimum`() {
        val prospectQualifier = mockkClass(ProspectQualifier::class)
        val minimumScore = 60
        val evaluator = ScoreQualificationEvaluator(
            prospectQualifier, minimumScore
        )

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