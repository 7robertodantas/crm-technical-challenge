package com.addi.business.evaluator.core

/**
 * This represents a command to trigger the lead evaluation.
 *
 * @see com.addi.business.service.LeadProspectServiceImpl
 */
data class PipelineParameters(
    val parameters: Map<EvaluationBucket, String>
) {
    fun get(bucket: EvaluationBucket): String = parameters.getOrDefault(bucket, "")
}
