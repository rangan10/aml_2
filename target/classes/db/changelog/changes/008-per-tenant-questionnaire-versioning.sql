--liquibase formatted sql

--changeset rangan10:008-per-tenant-questionnaire-versioning
-- =====================================================================
-- AML Module Schema - 008
-- Moves questionnaire versioning from the shared aml_questionnaire template
-- onto each tenant's own instance (aml_questionnaire_tenant). A questionnaire
-- (aml_questionnaire) is now a tenant-agnostic catalog entry (one row per
-- code); each adopting tenant owns its own versioned instance carrying its
-- version lineage, lifecycle (status/effective dates) and question config.
-- A tenant's structural change therefore versions that tenant's instance
-- only, never affecting other tenants. Responses now pin the exact
-- tenant-version they were answered against (questionnaire_tenant_id).
--
-- This is a clean rebuild of the questionnaire-assembly and response tables.
-- The question bank (aml_question / aml_question_option, with categories from
-- 006/007) is preserved; only the tables below are dropped and recreated,
-- then the KSHEMA questionnaire is re-seeded by re-attaching the existing
-- question rows.
-- =====================================================================

-- ==================== Drop old assembly/response tables (FK order) ====================

DROP TABLE IF EXISTS aml_question_response;
DROP TABLE IF EXISTS aml_questionnaire_response;
DROP TABLE IF EXISTS aml_tenant_questionnaire;
DROP TABLE IF EXISTS aml_questionnaire_tenant;
DROP TABLE IF EXISTS aml_questionnaire;

-- ==================== Questionnaire (shared catalog entry) ====================
-- One row per questionnaire_code; tenant-agnostic definition only.

CREATE TABLE aml_questionnaire (
    questionnaire_id     BIGSERIAL PRIMARY KEY,
    questionnaire_code    VARCHAR(50)  NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    description           VARCHAR(1000),
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_questionnaire_code ON aml_questionnaire(questionnaire_code);

-- ==================== Tenant questionnaire instance (versioned, per tenant) ====================
-- "Tenant T's version N of questionnaire X." Owns the tenant's version
-- lineage and lifecycle. The tenant's current instance is the ACTIVE row.

CREATE TABLE aml_questionnaire_tenant (
    questionnaire_tenant_id    BIGSERIAL PRIMARY KEY,
    questionnaire_id           BIGINT NOT NULL REFERENCES aml_questionnaire(questionnaire_id),
    tenant_id                  BIGINT NOT NULL REFERENCES tenant(tenant_id),
    version                    INT NOT NULL DEFAULT 1,
    status                     VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    effective_from             DATE NOT NULL,
    effective_to               DATE,
    previous_version_id        BIGINT REFERENCES aml_questionnaire_tenant(questionnaire_tenant_id),
    active                     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                 TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_questionnaire_tenant_questionnaire ON aml_questionnaire_tenant(questionnaire_id);
CREATE INDEX idx_questionnaire_tenant_tenant ON aml_questionnaire_tenant(tenant_id);
CREATE INDEX idx_questionnaire_tenant_status ON aml_questionnaire_tenant(status);
CREATE UNIQUE INDEX uq_questionnaire_tenant_version
    ON aml_questionnaire_tenant(questionnaire_id, tenant_id, version);

-- ==================== Tenant questionnaire configuration ====================
-- Maps questions (global or tenant-specific) onto a specific tenant
-- questionnaire version, carrying the tenant's per-question configuration.

CREATE TABLE aml_tenant_questionnaire (
    tenant_questionnaire_id     BIGSERIAL PRIMARY KEY,
    questionnaire_tenant_id      BIGINT NOT NULL REFERENCES aml_questionnaire_tenant(questionnaire_tenant_id),
    question_id                  BIGINT NOT NULL REFERENCES aml_question(question_id),
    mandatory                    BOOLEAN NOT NULL DEFAULT FALSE,
    display_order                INT NOT NULL DEFAULT 0,
    conditional_rule             VARCHAR(1000),
    active                       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_questionnaire_instance ON aml_tenant_questionnaire(questionnaire_tenant_id);
CREATE UNIQUE INDEX uq_tenant_questionnaire_question
    ON aml_tenant_questionnaire(questionnaire_tenant_id, question_id);

-- ==================== Questionnaire responses ====================

CREATE TABLE aml_questionnaire_response (
    response_id               BIGSERIAL PRIMARY KEY,
    tenant_id                 BIGINT NOT NULL REFERENCES tenant(tenant_id),
    questionnaire_tenant_id    BIGINT NOT NULL REFERENCES aml_questionnaire_tenant(questionnaire_tenant_id),
    customer_id               BIGINT NOT NULL REFERENCES customer(customer_id),
    policy_id                 BIGINT REFERENCES policy(policy_id),
    status                    VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at              TIMESTAMP,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_questionnaire_response_tenant ON aml_questionnaire_response(tenant_id);
CREATE INDEX idx_questionnaire_response_instance ON aml_questionnaire_response(questionnaire_tenant_id);
CREATE INDEX idx_questionnaire_response_customer ON aml_questionnaire_response(customer_id);

CREATE TABLE aml_question_response (
    question_response_id     BIGSERIAL PRIMARY KEY,
    response_id              BIGINT NOT NULL REFERENCES aml_questionnaire_response(response_id),
    tenant_id                BIGINT NOT NULL REFERENCES tenant(tenant_id),
    question_id              BIGINT NOT NULL REFERENCES aml_question(question_id),
    option_id                BIGINT REFERENCES aml_question_option(option_id),
    answer_text              VARCHAR(1000),
    created_at               TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_question_response_response ON aml_question_response(response_id);
CREATE INDEX idx_question_response_question ON aml_question_response(question_id);

-- ==================== Re-seed KSHEMA "AML Risk Assessment Questionnaire" ====================
-- Questions/options already exist in aml_question/aml_question_option and are
-- reused; here we recreate the catalog entry, the tenant's v1 instance, and
-- the 30 per-question config rows.

INSERT INTO aml_questionnaire (questionnaire_code, name, description)
SELECT 'AML_RISK_ASSESSMENT', 'AML Risk Assessment Questionnaire',
       'Customer-facing questionnaire capturing KYC/EDD data points used for AML risk assessment.'
WHERE NOT EXISTS (
    SELECT 1 FROM aml_questionnaire WHERE questionnaire_code = 'AML_RISK_ASSESSMENT'
);

INSERT INTO aml_questionnaire_tenant (questionnaire_id, tenant_id, version, status, effective_from)
SELECT qn.questionnaire_id, t.tenant_id, 1, 'ACTIVE', CURRENT_DATE
FROM aml_questionnaire qn
JOIN tenant t ON t.tenant_code = 'KSHEMA'
WHERE qn.questionnaire_code = 'AML_RISK_ASSESSMENT'
  AND NOT EXISTS (
      SELECT 1 FROM aml_questionnaire_tenant qt
      WHERE qt.questionnaire_id = qn.questionnaire_id AND qt.tenant_id = t.tenant_id AND qt.version = 1
  );

INSERT INTO aml_tenant_questionnaire (questionnaire_tenant_id, question_id, mandatory, display_order, conditional_rule)
SELECT qt.questionnaire_tenant_id, q.question_id, v.mandatory, v.display_order, v.conditional_rule
FROM aml_questionnaire_tenant qt
JOIN aml_questionnaire qn ON qn.questionnaire_id = qt.questionnaire_id AND qn.questionnaire_code = 'AML_RISK_ASSESSMENT'
JOIN tenant t ON t.tenant_id = qt.tenant_id AND t.tenant_code = 'KSHEMA'
CROSS JOIN (VALUES
        ('EMPLOYMENT_TYPE',                TRUE,  1,  NULL::text),
        ('COUNTRY_OF_RESIDENCE',           TRUE,  2,  NULL),
        ('NRI_FLAG',                       TRUE,  3,  NULL),
        ('PEP_FLAG',                       TRUE,  4,  NULL),
        ('RELATED_TO_PEP',                 TRUE,  5,  NULL),
        ('NGO_TRUST_AFFILIATION',          TRUE,  6,  NULL),
        ('ANNUAL_INCOME',                  TRUE,  7,  NULL),
        ('SOURCE_OF_INCOME',               TRUE,  8,  NULL),
        ('CURRENT_ADDRESS',                TRUE,  9,  NULL),
        ('PERMANENT_ADDRESS',              TRUE,  10, NULL),
        ('PAN_NUMBER',                     TRUE,  11, NULL),
        ('MOBILE_NUMBER',                  TRUE,  12, NULL),
        ('EMAIL_ADDRESS',                  TRUE,  13, NULL),
        ('CKYC_AVAILABLE',                 TRUE,  14, NULL),
        ('CKYC_KIN_NUMBER',                FALSE, 15, '{"dependsOnQuestionCode":"CKYC_AVAILABLE","operator":"EQUALS","expectedValue":"true"}'),
        ('SUM_INSURED',                    TRUE,  16, NULL),
        ('PREMIUM_AMOUNT',                 TRUE,  17, NULL),
        ('PAYMENT_MODE',                   TRUE,  18, NULL),
        ('PREMIUM_PAYER_TYPE',             TRUE,  19, NULL),
        ('PAYER_NAME',                     FALSE, 20, '{"dependsOnQuestionCode":"PREMIUM_PAYER_TYPE","operator":"EQUALS","expectedValue":"OTHER"}'),
        ('PAYER_PAN',                      FALSE, 21, '{"dependsOnQuestionCode":"PREMIUM_PAYER_TYPE","operator":"EQUALS","expectedValue":"OTHER"}'),
        ('RELATIONSHIP_TO_INSURED',        FALSE, 22, '{"dependsOnQuestionCode":"PREMIUM_PAYER_TYPE","operator":"IN","expectedValue":"NOMINEE,OTHER"}'),
        ('HYPOTHECATION_STATUS',           TRUE,  23, NULL),
        ('NOMINEE_NAME',                   TRUE,  24, NULL),
        ('NOMINEE_RELATIONSHIP',           TRUE,  25, NULL),
        ('ASSIGNEE_EXISTS',                TRUE,  26, NULL),
        ('BENEFICIAL_OWNER_FLAG',          TRUE,  27, NULL),
        ('ULTIMATE_BENEFICIAL_OWNER_NAME', FALSE, 28, '{"dependsOnQuestionCode":"BENEFICIAL_OWNER_FLAG","operator":"EQUALS","expectedValue":"true"}'),
        ('FOREIGN_SOURCE_OF_FUNDS',        TRUE,  29, NULL),
        ('FOREIGN_COUNTRY',                FALSE, 30, '{"dependsOnQuestionCode":"FOREIGN_SOURCE_OF_FUNDS","operator":"EQUALS","expectedValue":"true"}')
     ) AS v(question_code, mandatory, display_order, conditional_rule)
JOIN aml_question q ON q.tenant_id = t.tenant_id AND q.question_code = v.question_code
WHERE NOT EXISTS (
    SELECT 1 FROM aml_tenant_questionnaire existing
    WHERE existing.questionnaire_tenant_id = qt.questionnaire_tenant_id AND existing.question_id = q.question_id
);
