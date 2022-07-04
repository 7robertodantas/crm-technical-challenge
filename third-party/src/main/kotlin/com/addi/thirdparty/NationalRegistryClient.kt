package com.addi.thirdparty

import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException
import com.addi.business.adapter.NationalRegistry
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

@ExperimentalTime
class NationalRegistryClient(
    baseUrl: String
) : BaseClient(baseUrl, logger, errorHandler), NationalRegistry {

    companion object {
        private val logger = LoggerFactory.getLogger(NationalRegistryClient::class.java)
        private val errorHandler: ErrorHandler = { url, status, body ->
            when(status) {
                NOT_FOUND -> PersonNotFoundException("Could not perform request to get person at '$url'")
                else -> Exception("Could not perform request to get person registry at '$url'. response '$body'")
            }
        }
    }

    override suspend fun getRegistry(command: GetPersonDataCommand): PersonRegistry {
        return get("/persons/${command.nationalIdNumber}/registry")
    }
}