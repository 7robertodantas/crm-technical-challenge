package com.addi.business.thirdparty.adapter

import com.addi.business.domain.ProspectQualification
import com.addi.business.domain.command.GetProspectQualificationCommand

/**
 * This represents the internal prospect qualification system.
 */
interface ProspectQualifier {
    @Throws(Exception::class)
    suspend fun getScore(command: GetProspectQualificationCommand): ProspectQualification
}