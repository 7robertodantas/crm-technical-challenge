package com.addi.thirdparty

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.thirdparty.adapter.JudicialRecordArchive
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

@ExperimentalTime
class JudicialRecordArchiveClient(
    baseUrl: String
) : BaseClient(baseUrl, logger, errorHandler), JudicialRecordArchive {

    companion object {
        private val logger = LoggerFactory.getLogger(NationalRegistryClient::class.java)
        private val errorHandler: ErrorHandler = { url, status, body ->
            when(status) {
                NOT_FOUND -> PersonNotFoundException("Could not perform request to get judicial records at '$url'")
                else -> Exception("Could not perform request to get person judicial records at '$url'. response '$body'")
            }
        }
    }

    override suspend fun getRegistry(command: GetPersonDataCommand): JudicialRecord {
        return get("/persons/${command.nationalIdNumber}/judicial")
    }

}