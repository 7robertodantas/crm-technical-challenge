package com.addi.business.domain

import java.time.LocalDate

data class PersonRegistry(
    val nationalIdNumber: String,
    val birthDate: LocalDate,
    val firstName: String,
    val lastName: String,
    val email: String
)
