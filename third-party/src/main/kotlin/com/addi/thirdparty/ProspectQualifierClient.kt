package com.addi.thirdparty

import com.addi.business.domain.ProspectQualification
import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.adapter.ProspectQualifier
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ProspectQualifierClient(
    baseUrl: String
) : BaseClient(baseUrl, logger, errorHandler), ProspectQualifier {

    companion object {
        private val logger = LoggerFactory.getLogger(ProspectQualifierClient::class.java)
        private val errorHandler: ErrorHandler = { url, status, body ->
            when(status) {
                NOT_FOUND -> PersonNotFoundException("Could not perform request to get lead qualification at '$url'")
                else -> Exception("Could not perform request to get lead qualification at '$url'. response '$body'")
            }
        }
    }

    override suspend fun getScore(command: GetProspectQualificationCommand): ProspectQualification {
        return get("/persons/${command.nationalIdNumber}/score")
    }
}