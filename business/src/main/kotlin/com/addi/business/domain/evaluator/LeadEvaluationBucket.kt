package com.addi.business.domain.evaluator

import com.addi.evaluator.domain.EvaluationBucket

enum class LeadEvaluationBucket : EvaluationBucket {
    NATIONAL_ID_NUMBER,
    PERSON_EXISTS,
    PERSON_MATCHES_INTERNAL,
    PERSON_HAS_JUDICIAL_RECORDS,
    PERSON_HAS_SCORE_QUALIFICATION
}