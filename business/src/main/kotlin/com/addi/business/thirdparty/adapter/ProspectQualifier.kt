package com.addi.business.thirdparty.adapter

import com.addi.business.command.GetProspectQualificationCommand
import com.addi.business.thirdparty.dto.ProspectQualification

interface ProspectQualifier {
    suspend fun getScore(command: GetProspectQualificationCommand): ProspectQualification
}