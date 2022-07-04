package com.addi.business.domain

/**
 * This represents a judicial record
 * domain for the judicial record archive adapter.
 *
 * @see com.addi.business.thirdparty.adapter.JudicialRecordArchive
 */
data class JudicialRecord(
    val nationalIdNumber: String,
    val hasRecords: Boolean
)
