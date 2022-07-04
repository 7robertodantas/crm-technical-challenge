package com.addi.business.domain.command

/**
 * This represents a command to fetch a person's data.
 *
 * It can be used either to get the person's national registry and judicial records.
 *
 * @see com.addi.business.thirdparty.adapter.NationalRegistry
 * @see com.addi.business.thirdparty.adapter.JudicialRecordArchive
 */
data class GetPersonDataCommand(val nationalIdNumber: String)