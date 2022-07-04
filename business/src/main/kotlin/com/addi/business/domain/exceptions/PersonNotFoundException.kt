package com.addi.business.domain.exceptions

/**
 * This exception represents the case whereas the person does not exist.
 */
class PersonNotFoundException(message: String): Exception(message) {
}