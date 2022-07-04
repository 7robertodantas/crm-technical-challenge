package com.addi.business.evaluator

import com.addi.business.evaluator.core.EvaluationBucket

enum class LeadEvaluationBucket : EvaluationBucket {
    NATIONAL_ID_NUMBER,
    PERSON_EXISTS,
    PERSON_MATCHES_INTERNAL,
    PERSON_HAS_JUDICIAL_RECORDS,
    PERSON_HAS_SCORE_QUALIFICATION
}