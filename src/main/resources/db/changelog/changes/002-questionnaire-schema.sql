--liquibase formatted sql

--changeset rangan10:002-questionnaire-schema
-- =====================================================================
-- AML Module Schema - 002
-- Multi-tenant questionnaire configuration
-- =====================================================================

CREATE TABLE tenant (
    tenant_id       BIGSERIAL PRIMARY KEY,
    tenant_code     VARCHAR(30)  NOT NULL UNIQUE,
    tenant_name     VARCHAR(200) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ==================== Question bank ====================
-- A question with tenant_id = NULL is a global question, visible to every
-- tenant. A question with tenant_id set is only usable by that tenant.

CREATE TABLE aml_question (
    question_id     BIGSERIAL PRIMARY KEY,
    question_code   VARCHAR(50)  NOT NULL,
    question_text   VARCHAR(500) NOT NULL,
    question_type   VARCHAR(20)  NOT NULL,
    question_scope  VARCHAR(20)  NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE aml_question_option (
    option_id       BIGSERIAL PRIMARY KEY,
    question_id     BIGINT NOT NULL REFERENCES aml_question(question_id),
    option_code     VARCHAR(50)  NOT NULL,
    option_label    VARCHAR(200) NOT NULL,
    display_order   INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_question_option_question ON aml_question_option(question_id);
CREATE UNIQUE INDEX uq_question_option_code ON aml_question_option(question_id, option_code);

-- ==================== Questionnaire (versioned, per tenant) ====================
-- Each row is one immutable version of a tenant's questionnaire. Structural
-- changes (add/remove/modify question) create a new version once the prior
-- version already has customer responses recorded against it, so historical
-- responses keep referencing the exact version they were answered against.

CREATE TABLE aml_questionnaire (
    questionnaire_id     BIGSERIAL PRIMARY KEY,
    tenant_id             BIGINT NOT NULL REFERENCES tenant(tenant_id),
    questionnaire_code    VARCHAR(50)  NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    description           VARCHAR(1000),
    version               INT NOT NULL DEFAULT 1,
    status                VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    effective_from        DATE NOT NULL,
    effective_to          DATE,
    previous_version_id   BIGINT REFERENCES aml_questionnaire(questionnaire_id),
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_questionnaire_tenant ON aml_questionnaire(tenant_id);
CREATE INDEX idx_questionnaire_status ON aml_questionnaire(status);
CREATE UNIQUE INDEX uq_questionnaire_tenant_code_version ON aml_questionnaire(tenant_id, questionnaire_code, version);

-- ==================== Tenant questionnaire configuration ====================
-- Maps questions (global or tenant-specific) onto a specific questionnaire
-- version, carrying the tenant's configuration for that question.

CREATE TABLE aml_tenant_questionnaire (
    tenant_questionnaire_id     BIGSERIAL PRIMARY KEY,
    tenant_id                    BIGINT NOT NULL REFERENCES tenant(tenant_id),
    questionnaire_id             BIGINT NOT NULL REFERENCES aml_questionnaire(questionnaire_id),
    question_id                  BIGINT NOT NULL REFERENCES aml_question(question_id),
    mandatory                    BOOLEAN NOT NULL DEFAULT FALSE,
    display_order                INT NOT NULL DEFAULT 0,
    conditional_rule             VARCHAR(1000),
    active                        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_questionnaire_questionnaire ON aml_tenant_questionnaire(questionnaire_id);
CREATE INDEX idx_tenant_questionnaire_tenant ON aml_tenant_questionnaire(tenant_id);
CREATE UNIQUE INDEX uq_tenant_questionnaire_question ON aml_tenant_questionnaire(questionnaire_id, question_id);

-- ==================== Questionnaire responses ====================

CREATE TABLE aml_questionnaire_response (
    response_id         BIGSERIAL PRIMARY KEY,
    tenant_id            BIGINT NOT NULL REFERENCES tenant(tenant_id),
    questionnaire_id      BIGINT NOT NULL REFERENCES aml_questionnaire(questionnaire_id),
    customer_id           BIGINT NOT NULL REFERENCES customer(customer_id),
    policy_id             BIGINT REFERENCES policy(policy_id),
    status                VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at          TIMESTAMP,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_questionnaire_response_tenant ON aml_questionnaire_response(tenant_id);
CREATE INDEX idx_questionnaire_response_questionnaire ON aml_questionnaire_response(questionnaire_id);
CREATE INDEX idx_questionnaire_response_customer ON aml_questionnaire_response(customer_id);
