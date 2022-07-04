package com.addi.business.steps

import com.addi.business.domain.evaluator.LeadEvaluationBucket
import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.evaluator.core.PipelineParameters
import com.addi.evaluator.core.EvaluationOutcome
import com.addi.business.adapter.ProspectQualifier
import com.addi.business.domain.ProspectQualification
import com.addi.business.domain.evaluator.LeadEvaluationBucket.PERSON_HAS_SCORE_QUALIFICATION
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ScoreQualificationEvaluatorTest {

    private val nationalIdNumber = "c02f4e2c"
    private val leadEvaluateCommand = PipelineParameters(mapOf(
        LeadEvaluationBucket.NATIONAL_ID_NUMBER to nationalIdNumber
    ))
    private val getScoreCommand = GetProspectQualificationCommand(nationalIdNumber)

    private val prospectQualifier = mockkClass(ProspectQualifier::class)
    private val minimumScore = 60
    private val evaluator = ScoreQualificationEvaluatorStep(
        prospectQualifier, minimumScore
    )

    @Test
    fun `it should fail if some exception is thrown`() {
        val exception = Exception("oh no!")
        coEvery { prospectQualifier.getScore(eq(getScoreCommand)) } throws exception
        val result = runBlocking { evaluator.evaluate(leadEvaluateCommand) }
        assertThat(result.success).isFalse
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
            assertThat(result.success).isFalse
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
            assertThat(result.success).isTrue
            assertThat(result.isFail()).isFalse
            assertThat(result.isSuccess()).isTrue
            assertThat(result).isEqualTo(
                EvaluationOutcome.success(
                    mapOf(
                        PERSON_HAS_SCORE_QUALIFICATION to "true"
                    )
                )
            )
        }
    }

}