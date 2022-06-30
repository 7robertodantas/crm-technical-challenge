package com.addi.business.thirdparty.adapter

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.thirdparty.dto.JudicialRecord

interface JudicialRecordArchive {
    suspend fun getRegistry(command: GetPersonDataCommand): JudicialRecord
}