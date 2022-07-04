package com.addi.business.adapter

import com.addi.business.domain.Person

/**
 * This represents the local database for person.
 */
interface PersonRepository {
    suspend fun matchStored(person: Person): Boolean
}