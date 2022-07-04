package com.addi.business.thirdparty.adapter

import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.PersonRegistry


/**
 * This represents the national registry identification external system
 */
interface NationalRegistry {
    suspend fun getRegistry(command: GetPersonDataCommand): PersonRegistry?
}