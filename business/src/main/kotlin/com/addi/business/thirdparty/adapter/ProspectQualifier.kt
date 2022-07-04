package com.addi.business.thirdparty.adapter

import com.addi.business.domain.command.GetProspectQualificationCommand
import com.addi.business.domain.ProspectQualification

/**
 * This represents the internal prospect qualification system.
 */
interface ProspectQualifier {
    suspend fun getScore(command: GetProspectQualificationCommand): ProspectQualification
}