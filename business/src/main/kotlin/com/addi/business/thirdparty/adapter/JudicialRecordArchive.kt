package com.addi.business.thirdparty.adapter

import com.addi.business.domain.JudicialRecord
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException

/**
 * This represents the national archives' external system.
 */
interface JudicialRecordArchive {
    @Throws(PersonNotFoundException::class, Exception::class)
    suspend fun getRegistry(command: GetPersonDataCommand): JudicialRecord
}