package com.addi.evaluator.domain

import com.addi.evaluator.domain.EvaluationBucket

/**
 * This represents a data that passes through each step
 */
data class PipelineParameters(
    val parameters: Map<EvaluationBucket, String>
) {
    fun get(bucket: EvaluationBucket): String = parameters.getOrDefault(bucket, "")
}
