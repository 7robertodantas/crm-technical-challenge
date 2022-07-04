package com.addi.business.domain

/**
 * This represents a prospect qualification result
 * domain for the prospect qualifier adapter.
 *
 * @see com.addi.business.thirdparty.adapter.ProspectQualifier
 */
data class ProspectQualification(
    val score: Int
)
