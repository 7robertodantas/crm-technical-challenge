package com.addi.business.thirdparty.adapter

import com.addi.business.command.GetPersonDataCommand
import com.addi.business.thirdparty.dto.PersonRegistry

interface NationalRegistry {
    suspend fun getRegistry(command: GetPersonDataCommand): PersonRegistry
}