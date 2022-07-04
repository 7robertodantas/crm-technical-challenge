package com.addi.business.thirdparty.adapter

import com.addi.business.domain.Person

interface PersonRepository {
    suspend fun matchStored(person: Person): Boolean
}