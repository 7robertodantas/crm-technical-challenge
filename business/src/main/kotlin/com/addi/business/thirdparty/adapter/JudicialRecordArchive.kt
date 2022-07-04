package com.addi.business.thirdparty.adapter

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.JudicialRecord

/**
 * This represents the national archives' external system.
 */
interface JudicialRecordArchive {
    suspend fun getRegistry(command: GetPersonDataCommand): JudicialRecord
}