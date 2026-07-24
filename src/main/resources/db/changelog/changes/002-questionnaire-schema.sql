-- =====================================================
-- AML QUESTION
-- =====================================================

CREATE TABLE aml_question (
    question_id BIGSERIAL PRIMARY KEY,
    question_code VARCHAR(50) NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_scope VARCHAR(20) NOT NULL,
    question_category VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_aml_question_code UNIQUE (question_code)
);

CREATE INDEX idx_aml_question_active
    ON aml_question(active);

CREATE INDEX idx_aml_question_scope
    ON aml_question(question_scope);

CREATE INDEX idx_aml_question_category
    ON aml_question(question_category);


-- =====================================================
-- AML QUESTION OPTION
-- =====================================================

CREATE TABLE aml_question_option (
    option_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_code VARCHAR(50) NOT NULL,
    option_label VARCHAR(200) NOT NULL,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_aml_question_option_question
        FOREIGN KEY (question_id)
        REFERENCES aml_question(question_id)
        ON DELETE CASCADE,

    CONSTRAINT uk_question_option_code
        UNIQUE (question_id, option_code)
);

CREATE INDEX idx_aml_question_option_question
    ON aml_question_option(question_id);

CREATE INDEX idx_aml_question_option_display_order
    ON aml_question_option(question_id, display_order);


-- =====================================================
-- AML QUESTION RESPONSE
-- =====================================================

CREATE TABLE aml_question_response (
    question_response_id BIGSERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_question_response_question
        FOREIGN KEY (question_id)
        REFERENCES aml_question(question_id)
);

CREATE INDEX idx_question_response_question
    ON aml_question_response(question_id);

CREATE INDEX idx_question_response_user
    ON aml_question_response(user_profile_id);

CREATE INDEX idx_question_response_tenant
    ON aml_question_response(tenant_id);

CREATE INDEX idx_question_response_user_question
    ON aml_question_response(user_profile_id, question_id);

CREATE INDEX idx_question_response_tenant_question
    ON aml_question_response(tenant_id, question_id);


-- =====================================================
-- AML QUESTION OPTION RESPONSE
-- =====================================================

CREATE TABLE aml_question_option_response (
    option_response_id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,

    CONSTRAINT fk_option_response_response
        FOREIGN KEY (response_id)
        REFERENCES aml_question_response(question_response_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_option_response_option
        FOREIGN KEY (option_id)
        REFERENCES aml_question_option(option_id),

    CONSTRAINT uk_option_response
        UNIQUE (response_id, option_id)
);

CREATE INDEX idx_option_response_response
    ON aml_question_option_response(response_id);

CREATE INDEX idx_option_response_option
    ON aml_question_option_response(option_id);


-- =====================================================
-- AML QUESTION TENANT
-- =====================================================

CREATE TABLE aml_question_tenant (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,

    CONSTRAINT fk_question_tenant_question
        FOREIGN KEY (question_id)
        REFERENCES aml_question(question_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_question_tenant_question
    ON aml_question_tenant(question_id);

CREATE INDEX idx_question_tenant_tenant
    ON aml_question_tenant(tenant_id);

CREATE INDEX idx_question_tenant_active
    ON aml_question_tenant(active);

CREATE UNIQUE INDEX uk_question_tenant_version
    ON aml_question_tenant(question_id, tenant_id, version_no);


-- =====================================================
-- ASSIGNEE CHANGE LOG
-- =====================================================

CREATE TABLE assignee_change_log (
    change_id BIGSERIAL PRIMARY KEY,
    change_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_assignee_change_log_date
    ON assignee_change_log(change_date);