package com.addi.business.domain.command

/**
 * This represents a command to get a score qualification of a lead.
 *
 * It can be used either to get the person's national registry and judicial records.
 */
data class GetProspectQualificationCommand(val nationalIdNumber: String)
