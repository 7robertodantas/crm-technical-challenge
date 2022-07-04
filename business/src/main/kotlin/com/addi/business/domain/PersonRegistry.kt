package com.addi.business.domain

import java.time.LocalDate

/**
 * This represents a person registry that is used
 * as domain for national registry adapter.
 *
 * @see com.addi.business.thirdparty.adapter.NationalRegistry
 */
data class PersonRegistry(
    val nationalIdNumber: String,
    val birthDate: LocalDate,
    val firstName: String,
    val lastName: String,
    val email: String
)
