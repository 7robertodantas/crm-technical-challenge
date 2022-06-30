package com.addi.business.database

import com.addi.business.domain.Person

interface PersonDatabase {
    suspend fun getByNationalIdentifier(nationalIdNumber: String): Person
}