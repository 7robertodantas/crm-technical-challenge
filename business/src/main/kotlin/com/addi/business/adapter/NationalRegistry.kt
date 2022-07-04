package com.addi.business.adapter

import com.addi.business.domain.PersonRegistry
import com.addi.business.domain.command.GetPersonDataCommand
import com.addi.business.domain.exceptions.PersonNotFoundException


/**
 * This represents the national registry identification external system
 */
interface NationalRegistry {
    @Throws(PersonNotFoundException::class, Exception::class)
    suspend fun getRegistry(command: GetPersonDataCommand): PersonRegistry
}