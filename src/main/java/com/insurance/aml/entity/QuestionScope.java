package com.insurance.aml.entity;

/**
 * GLOBAL questions are available to every tenant. TENANT_SPECIFIC questions
 * are only usable within the questionnaires of the tenant that owns them.
 */
public enum QuestionScope {
    GLOBAL,
    TENANT_SPECIFIC
}
