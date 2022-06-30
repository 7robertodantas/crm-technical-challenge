package com.addi.business.database

import com.addi.business.domain.Person

interface PersonRepository {
    suspend fun matchStored(person: Person): Boolean
}