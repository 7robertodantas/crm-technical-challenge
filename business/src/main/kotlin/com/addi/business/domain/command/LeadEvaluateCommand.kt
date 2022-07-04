package com.addi.business.domain.command

/**
 * This represents a command to trigger the lead evaluation.
 *
 * @see com.addi.business.service.LeadProspectServiceImpl
 */
data class LeadEvaluateCommand(
    val nationalIdNumber: String
)
