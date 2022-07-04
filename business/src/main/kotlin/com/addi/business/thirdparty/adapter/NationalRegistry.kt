package com.addi.business.thirdparty.adapter

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.PersonRegistry

interface NationalRegistry {
    suspend fun getRegistry(command: GetPersonDataCommand): PersonRegistry
}