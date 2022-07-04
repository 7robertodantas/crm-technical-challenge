package com.addi.business.service

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.ProspectQualification
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.evaluator.core.PipelineParameters
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.evaluator.core.EvaluationBucket
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import com.addi.business.thirdparty.adapter.NationalRegistry
import com.addi.business.thirdparty.adapter.PersonRepository
import com.addi.business.thirdparty.adapter.ProspectQualifier
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
internal class LeadProspectServiceImplTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val nationalRegistry: NationalRegistry = mockk()
    private val personRepository: PersonRepository = mockk()
    private val judicialRecordArchive: JudicialRecordArchive = mockk()
    private val prospectQualifier: ProspectQualifier = mockk()

    private val leadProspectService: LeadProspectService = LeadProspectServiceImpl(
        nationalRegistry,
        personRepository,
        judicialRecordArchive,
        prospectQualifier,
        testDispatcher
    )

    private val nationalIdNumber = "b4c28fef"
    private val leadEvaluateCommand = PipelineParameters(mapOf(
        EvaluationBucket.NATIONAL_ID_NUMBER to nationalIdNumber
    ))
    private val getPersonDataCommand = GetPersonDataCommand(nationalIdNumber)
    private val getProspectCommand = GetProspectQualificationCommand(nationalIdNumber)

    private val personRegistry = PersonRegistry(
        nationalIdNumber = nationalIdNumber,
        birthDate = LocalDate.now(),
        firstName = "intend",
        lastName = "bill",
        email = "test@email.com"
    )

    @Test
    fun `it should fail if person does not exist on national registry`() = testScope.runBlockingTest {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } throws PersonNotFoundException("could not find person")
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(nationalIdNumber, false)

        val result = leadProspectService.evaluate(leadEvaluateCommand)
        assertThat(result.isFail())
        assertThat(result.error).contains("person does not exist")
    }

    @Test
    fun `it should fail if person does not match stored`() = testScope.runBlockingTest {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns personRegistry
        coEvery { personRepository.matchStored(any()) } returns false
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(nationalIdNumber, false)

        val result = leadProspectService.evaluate(leadEvaluateCommand)
        assertThat(result.isFail())
        assertThat(result.error).contains("personal information does not match")
    }

    @Test
    fun `it should fail if person has judicial records`() = testScope.runBlockingTest {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns personRegistry
        coEvery { personRepository.matchStored(any()) } returns true
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(nationalIdNumber, true)

        val result = leadProspectService.evaluate(leadEvaluateCommand)
        assertThat(result.isFail())
        assertThat(result.error).contains("person has judicial records")
    }

    @Test
    fun `it should fail if internal prospect qualification system gives score below 60`() = testScope.runBlockingTest {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns personRegistry
        coEvery { personRepository.matchStored(any()) } returns true
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(nationalIdNumber, false)
        coEvery { prospectQualifier.getScore(eq(getProspectCommand)) } returns ProspectQualification(50)

        val result = leadProspectService.evaluate(leadEvaluateCommand)
        assertThat(result.isFail())
        assertThat(result.error).contains("below minimum score")
    }

    @Test
    fun `it should succeeded if matches all evaluations`() = testScope.runBlockingTest {
        coEvery { nationalRegistry.getRegistry(eq(getPersonDataCommand)) } returns personRegistry
        coEvery { personRepository.matchStored(any()) } returns true
        coEvery { judicialRecordArchive.getRegistry(eq(getPersonDataCommand)) } returns JudicialRecord(nationalIdNumber, false)
        coEvery { prospectQualifier.getScore(eq(getProspectCommand)) } returns ProspectQualification(90)

        val result = leadProspectService.evaluate(leadEvaluateCommand)
        assertThat(result.isSuccess())
        assertThat(result.error).isNullOrEmpty()
    }
}